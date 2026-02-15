package com.rb.repoinsight;

import java.nio.file.Path;

import com.rb.repoinsight.ai.CopilotClient;
import com.rb.repoinsight.model.RepoContext;
import com.rb.repoinsight.report.MarkdownReportGenerator;
import com.rb.repoinsight.scan.ComplexityAnalyzer;
import com.rb.repoinsight.scan.JaCoCoParser;
import com.rb.repoinsight.scan.RepoMetrics;
import com.rb.repoinsight.scan.RepoMetricsCollector;
import com.rb.repoinsight.scan.TestCoverageCalculator;
import com.rb.repoinsight.scanner.RepoScanner;
import com.rb.repoinsight.service.AnalysisOrchestrator;
import com.rb.repoinsight.util.ProcessBuilderCommandExecutor;

public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        // Parse arguments
        boolean enableAi = true;  // AI is enabled by default
        boolean enableAccurateCoverage = false;
        
        for (String arg : args) {
            if ("--disable-ai".equals(arg)) {
                enableAi = false;
            } else if ("--coverage-accurate".equals(arg)) {
                enableAccurateCoverage = true;
            }
        }

        Path repoRoot = Path.of(".").toAbsolutePath().normalize();

        System.out.println("Copilot Repo Insight");
        System.out.println("Analyzing repository: " + repoRoot);
        System.out.println();

        RepoScanner scanner = new RepoScanner();
        RepoContext context = scanner.scan(repoRoot);

        // Always calculate test coverage and complexity (deterministic)
        RepoMetricsCollector metricsCollector = new RepoMetricsCollector();
        RepoMetrics metrics = metricsCollector.collect(repoRoot);
        
        int coverage;
        boolean accurateCoverage = false;
        
        if (enableAccurateCoverage) {
            // Check if JaCoCo report already exists
            int jacocoCoverage = JaCoCoParser.parseJaCoCoReport(repoRoot);
            if (jacocoCoverage >= 0) {
                coverage = jacocoCoverage;
                accurateCoverage = true;
                System.out.println("JaCoCo coverage found: " + coverage + "%");
            } else {
                // Try to run JaCoCo for accurate coverage
                if (JaCoCoParser.runJaCoCoAnalysis(repoRoot)) {
                    jacocoCoverage = JaCoCoParser.parseJaCoCoReport(repoRoot);
                    if (jacocoCoverage >= 0) {
                        coverage = jacocoCoverage;
                        accurateCoverage = true;
                        System.out.println("JaCoCo coverage: " + coverage + "%");
                    } else {
                        // Fall back to heuristic if parsing failed
                        coverage = TestCoverageCalculator.calculateApproximateCoverage(
                            metrics.getTotalClasses(), 
                            metrics.getTotalTestClasses());
                        System.out.println("Falling back to heuristic coverage: " + coverage + "%");
                    }
                } else {
                    // Fall back to heuristic if JaCoCo failed
                    coverage = TestCoverageCalculator.calculateApproximateCoverage(
                        metrics.getTotalClasses(), 
                        metrics.getTotalTestClasses());
                    System.out.println("JaCoCo analysis failed, using heuristic coverage: " + coverage + "%");
                }
            }
        } else {
            // Use heuristic (always runs)
            coverage = TestCoverageCalculator.calculateApproximateCoverage(
                metrics.getTotalClasses(), 
                metrics.getTotalTestClasses());
        }
        
        context.setTestCoveragePercentage(coverage);
        context.setAccurateCoverageAvailable(accurateCoverage);
        ComplexityAnalyzer.analyze(context, metrics);

        // Analyze project architecture (always runs, independent of AI)
        System.out.println("Running architecture analysis...");
        CopilotClient aiClient = new CopilotClient(new ProcessBuilderCommandExecutor());
        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(aiClient, metricsCollector);
        orchestrator.analyzeArchitecture(context, repoRoot);
        System.out.println("Architecture analysis complete");

        // AI Analysis (opt-in)
        if (enableAi) {
            System.out.println("Running AI analysis...");
            orchestrator.performAnalysis(context, repoRoot);
            System.out.println("AI analysis complete");
        }

        printSummary(context);

        MarkdownReportGenerator reportGenerator = new MarkdownReportGenerator();
        Path outputFile = Path.of("repo-insight.md");
        reportGenerator.generate(context, outputFile);

        System.out.println("Report generated: " + outputFile.toAbsolutePath());

        long elapsedTime = System.currentTimeMillis() - startTime;
        context.setGenerationTime(elapsedTime);
    }

    private static void printSummary(RepoContext context) {
        System.out.println("Detected:");
        System.out.println("- Build tool: " + context.getBuildTool());
        System.out.println("- Language: " + context.getLanguage());
        System.out.println("- Tests present: " + context.hasTests());
        System.out.println("- CI present: " + context.hasCi());
    }
}

