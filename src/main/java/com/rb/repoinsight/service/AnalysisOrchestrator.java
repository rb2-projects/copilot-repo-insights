package com.rb.repoinsight.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import com.rb.repoinsight.ai.AiClient;
import com.rb.repoinsight.constants.PromptsConfig;
import com.rb.repoinsight.model.RepoContext;
import com.rb.repoinsight.scan.ArchitectureAnalyzer;
import com.rb.repoinsight.scan.ComplexityAnalyzer;
import com.rb.repoinsight.scan.RepoMetrics;
import com.rb.repoinsight.scan.RepoMetricsCollector;
import com.rb.repoinsight.scan.TestCoverageCalculator;

/**
 * Orchestrates the AI-enhanced analysis workflow.
 */
public class AnalysisOrchestrator {

    private static final int MAX_PROMPT_LENGTH = 8000;

    private final AiClient aiClient;
    private final RepoMetricsCollector metricsCollector;

    public AnalysisOrchestrator(AiClient aiClient,
            RepoMetricsCollector metricsCollector) {
        this.aiClient = aiClient;
        this.metricsCollector = metricsCollector;
    }

    public void performAnalysis(RepoContext context, Path repoRoot) {

        if (!aiClient.isAvailable()) {
            context.setCopilotAvailable(false);
            context.setCopilotFailureReason(aiClient.getUnavailabilityReason());
            System.out.println("AI Analysis skipped: " + aiClient.getUnavailabilityReason());
            return;
        }

        System.out.println("Sending repository metrics to GitHub Copilot...");

        try {
            RepoMetrics metrics = metricsCollector.collect(repoRoot);
            
            // Calculate test coverage (always run heuristic)
            int coverage = TestCoverageCalculator.calculateApproximateCoverage(
                metrics.getTotalClasses(), 
                metrics.getTotalTestClasses());
            context.setTestCoveragePercentage(coverage);
            
            // Analyze complexity and maintainability
            ComplexityAnalyzer.analyze(context, metrics);
            
            String prompt = loadAndPopulateTemplate(context, metrics);

            if (prompt.length() > MAX_PROMPT_LENGTH) {
                System.out.println("Warning: AI prompt truncated to "
                        + MAX_PROMPT_LENGTH + " characters.");
                prompt = truncatePrompt(prompt, MAX_PROMPT_LENGTH);
            }

            String analysis = aiClient.analyze(prompt);

            if (analysis == null || analysis.trim().isEmpty()) {
                context.setCopilotAvailable(false);
                context.setCopilotFailureReason("Empty response from Copilot.");
                return;
            }

            if (analysis.startsWith("AI Analysis Failed")
                    || analysis.startsWith("AI Analysis Unavailable")) {
                context.setCopilotAvailable(false);
                context.setCopilotFailureReason(analysis);
                return;
            }

            context.setCopilotAvailable(true);
            context.setCopilotOutput(cleanCopilotOutput(analysis.trim()));

        } catch (IOException e) {
            context.setCopilotAvailable(false);
            context.setCopilotFailureReason("AI Analysis Failed: " + e.getMessage());
            System.err.println("AI Analysis error: " + e.getMessage());
        }
    }

    /**
     * Clean up Copilot CLI debug output.
     * Removes reasoning steps, intermediate processing messages, and PowerShell artifacts.
     * Keeps only the final analysis and structured content.
     */
    private String cleanCopilotOutput(String output) {
        if (output == null || output.isBlank()) {
            return output;
        }

        // First pass: find where real content starts (## or ### headers)
        String[] lines = output.split("\n");
        int contentStartIndex = -1;
        
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.startsWith("## ") || trimmed.startsWith("### ")) {
                contentStartIndex = i;
                break;
            }
        }

        // If no headers found, look for other content indicators
        if (contentStartIndex == -1) {
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (trimmed.startsWith("| ") || trimmed.startsWith("✅") || 
                    trimmed.startsWith("- [") || trimmed.startsWith("- **")) {
                    contentStartIndex = i - 1; // Include header line if exists
                    break;
                }
            }
        }

        // Build result from content start
        StringBuilder cleaned = new StringBuilder();
        int startIdx = Math.max(0, contentStartIndex);
        
        for (int i = startIdx; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            // Skip empty lines
            if (trimmed.isEmpty()) {
                continue;
            }

            // Skip remaining debug markers
            if (trimmed.startsWith("●") || trimmed.startsWith("✗") || 
                trimmed.startsWith("$") || trimmed.startsWith("Let me ") || 
                trimmed.startsWith("Now ") || trimmed.startsWith("I'll ") || 
                trimmed.startsWith("Excellent! ") || trimmed.startsWith("Perfect! ")) {
                continue;
            }

            // Skip PowerShell output
            if (trimmed.contains("Measure-Object") || trimmed.contains("Select-Object") || 
                trimmed.contains("Get-ChildItem") || trimmed.contains("Write-Host") ||
                trimmed.contains("Where-Object") || trimmed.contains("cd ") ||
                trimmed.contains("Permission denied") || trimmed.contains("operable program") ||
                trimmed.contains("pwsh.exe") || trimmed.contains("is not recognized") ||
                trimmed.contains("CategoryInfo") || trimmed.contains("FullyQualifiedTypeId")) {
                continue;
            }

            // Add the line
            if (cleaned.length() > 0) {
                cleaned.append("\n");
            }
            cleaned.append(line);
        }

        return cleaned.toString().trim();
    }

    /**
     * Analyze project architecture independently of AI.
     * This runs even when AI is not available.
     */
    public void analyzeArchitecture(RepoContext context, Path repoRoot) {
        try {
            ArchitectureAnalyzer.analyze(repoRoot, context);
            
            // Enhance with AI descriptions if available
            if (context.isCopilotAvailable()) {
                ArchitectureAnalyzer.enhanceWithAiDescriptions(repoRoot, context);
            }
        } catch (Exception e) {
            System.err.println("Architecture analysis error: " + e.getMessage());
            // Best-effort, continue without architecture data
        }
    }

    private String loadAndPopulateTemplate(RepoContext context,
            RepoMetrics metrics) throws IOException {

        String template = PromptsConfig.COMPREHENSIVE_ANALYSIS_TEMPLATE;

        template = template.replace("{{projectName}}",
                context.getRepoPath() != null ? context.getRepoPath() : "Unknown");

        template = template.replace("{{buildTool}}",
                context.getBuildTool() != null ? context.getBuildTool() : "Unknown");

        template = template.replace("{{language}}",
                context.getLanguage() != null ? context.getLanguage() : "Unknown");

        template = template.replace("{{packaging}}",
                context.getPackagingType() != null ? context.getPackagingType() : "Unknown");

        template = template.replace("{{totalFiles}}", String.valueOf(metrics.getTotalFiles()));
        template = template.replace("{{totalClasses}}", String.valueOf(metrics.getTotalClasses()));
        template = template.replace("{{totalTestClasses}}", String.valueOf(metrics.getTotalTestClasses()));
        template = template.replace("{{approximateLinesOfCode}}",
                String.valueOf(metrics.getApproximateLinesOfCode()));

        String packages = metrics.getTopLevelPackages().isEmpty()
                ? "None detected"
                : String.join(", ", metrics.getTopLevelPackages());

        template = template.replace("{{topLevelPackages}}", packages);

        String largestFiles = metrics.getLargestFiles().isEmpty()
                ? "None detected"
                : metrics.getLargestFiles().stream()
                        .map(f -> f.getRelativePath() + " (" + f.getLineCount() + " LOC)")
                        .collect(Collectors.joining("\n"));

        template = template.replace("{{largestFiles}}", largestFiles);

        String frameworks = context.isUsesSpring()
                ? "Spring Framework"
                : "None detected";

        template = template.replace("{{frameworks}}", frameworks);

        String dependencies = context.getExternalDependencies().isEmpty()
                ? "None detected"
                : context.getExternalDependencies().stream()
                        .map(d -> d.getCategory() + ": " + d.getName())
                        .collect(Collectors.joining(", "));

        template = template.replace("{{externalDependencies}}", dependencies);

        template = template.replace("{{testsPresent}}", String.valueOf(context.hasTests()));
        template = template.replace("{{ciPresent}}", String.valueOf(context.hasCi()));

        return template;
    }

    private String truncatePrompt(String prompt, int maxLength) {
        if (prompt.length() <= maxLength) {
            return prompt;
        }
        return prompt.substring(0, maxLength - 50)
                + "\n\n[Truncated due to size limits]";
    }
}
