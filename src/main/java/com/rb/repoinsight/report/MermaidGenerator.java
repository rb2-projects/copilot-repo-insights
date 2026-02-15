package com.rb.repoinsight.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.rb.repoinsight.model.ExternalDependency;
import com.rb.repoinsight.model.RepoContext;

/**
 * Generates Mermaid diagrams for the repository insight report.
 */
public class MermaidGenerator {

    public static String generate(RepoContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("### Project Architecture\n");
        if (!context.getProjectModules().isEmpty()) {
            sb.append("```mermaid\n").append(generateProjectArchitecture(context)).append("```\n\n");
        } else {
            sb.append("_No module structure detected._\n\n");
        }

        String systemContext = generateSystemContext(context);
        if (!systemContext.isEmpty()) {
            sb.append("### System Context\n");
            sb.append("```mermaid\n").append(systemContext).append("```\n\n");
        }

        return sb.toString();
    }

    private static String generateFindingsOverlay(RepoContext context) {
        List<String> hygiene = FindingsGenerator.collectHygieneFindings(context);
        List<String> recommendations = FindingsGenerator.collectRecommendations(context);

        // Filter: Keep only negative hygiene findings and all recommendations
        List<String> filteredHygiene = hygiene.stream()
                .filter(f -> f.toLowerCase().contains("no ") || f.toLowerCase().contains("not detected"))
                .collect(Collectors.toList());

        // We exclude integration findings (e.g., "The project integrates with...")
        // as they are positive confirmations, unless the user specifically wants them.
        // User said: "negative hygiene findings and recommendations only".

        if (filteredHygiene.isEmpty() && recommendations.isEmpty()) {
            return "";
        }

        Map<String, List<String>> targetToAnnotations = new LinkedHashMap<>();

        // Process findings first to ensure they appear before recommendations within
        // each target
        for (String f : filteredHygiene) {
            String target = inferTarget(f);
            targetToAnnotations.computeIfAbsent(target, k -> new ArrayList<>()).add(f);
        }

        for (String r : recommendations) {
            String target = inferTarget(r);
            targetToAnnotations.computeIfAbsent(target, k -> new ArrayList<>()).add(r);
        }

        if (targetToAnnotations.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        sb.append("  Project[Project]\n");

        // Deterministic target ordering: Hygiene (Tests, CI) then Integration (Alpha)
        List<String> sortedTargets = targetToAnnotations.keySet().stream()
                .sorted((a, b) -> {
                    if (isHygiene(a) && !isHygiene(b))
                        return -1;
                    if (!isHygiene(a) && isHygiene(b))
                        return 1;
                    return a.compareTo(b);
                })
                .collect(Collectors.toList());

        int idCounter = 1;
        for (String target : sortedTargets) {
            String targetNodeId = target.replaceAll("\\s+", "");
            sb.append("  Project --> ").append(targetNodeId).append("[").append(target).append("]\n");

            for (String annotation : targetToAnnotations.get(target)) {
                String annotationId = "A" + (idCounter++);
                // Mermaid node with square brackets and escaped quotes
                sb.append("  ").append(targetNodeId).append(" --- ").append(annotationId)
                        .append("[\"").append(annotation.replace("\"", "'")).append("\"]\n");
            }
        }

        return sb.toString();
    }

    private static String inferTarget(String text) {
        String lower = text.toLowerCase();

        // Specific integration targets take precedence
        // (e.g. "local/CI persistence" should go to Persistence, not CI)
        if (lower.contains("persistence") || lower.contains("database"))
            return "Persistence";
        if (lower.contains("messaging") || lower.contains("message"))
            return "Messaging";
        if (lower.contains("cloud"))
            return "Cloud Services";
        if (lower.contains("http api") || lower.contains("external systems via http") || lower.contains("stubs")
                || lower.contains("contract tests"))
            return "Web";

        // CI keywords
        if (lower.contains("ci configuration") || lower.contains("ci pipeline") || lower.contains("ci "))
            return "CI";

        // Fallback for generic test mentions
        if (lower.contains("test"))
            return "Tests";

        return "General";
    }

    private static boolean isHygiene(String target) {
        return "Tests".equals(target) || "CI".equals(target);
    }

    private static String generateSystemContext(RepoContext context) {
        if (context.getExternalDependencies().isEmpty()) {
            return "";
        }

        // Collect unique categories sorted alphabetically by display name
        Set<String> categories = context.getExternalDependencies().stream()
                .map(ExternalDependency::getCategory)
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        StringBuilder sb = new StringBuilder();
        sb.append("graph TD\n");
        sb.append("  Project[Project]\n");

        for (String category : categories) {
            String nodeId = category.replaceAll("\\s+", "");
            sb.append("  Project -->|").append(category).append("| ").append(nodeId).append("[").append(category)
                    .append("]\n");
        }

        return sb.toString();
    }

    private static String generateHygieneOverview(RepoContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph LR\n");
        sb.append("  Project[Project]\n");
        sb.append("  Tests[Tests]\n");
        sb.append("  CI[CI]\n");

        // Tests edge
        if (context.hasTests()) {
            sb.append("  Project -- \"Present\" --> Tests\n");
        } else {
            sb.append("  Project -. \"Absent\" .-> Tests\n");
        }

        // CI edge
        if (context.hasCi()) {
            sb.append("  Project -- \"Present\" --> CI\n");
        } else {
            sb.append("  Project -. \"Absent\" .-> CI\n");
        }

        return sb.toString();
    }
    private static String generateProjectArchitecture(RepoContext context) {
        if (context.getProjectModules().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("graph TB\n");

        if (context.getProjectModules().size() == 1) {
            // Single module - show packages
            var module = context.getProjectModules().get(0);
            sb.append("  Project[\"").append(module.getName()).append("\"]\n");
            
            if (!module.getTopLevelPackages().isEmpty()) {
                for (String pkg : module.getTopLevelPackages()) {
                    String nodeId = pkg.replaceAll("[^a-zA-Z0-9]", "");
                    sb.append("  Project --> ").append(nodeId).append("[\"").append(pkg).append("\"]\n");
                }
            } else {
                sb.append("  Project --> Packages[\"Source packages\"]\n");
            }
        } else {
            // Multi-module - show modules and their packages
            sb.append("  Project[\"Project\"]\n");
            
            for (var module : context.getProjectModules()) {
                String moduleNodeId = module.getName().replaceAll("[^a-zA-Z0-9]", "");
                String moduleLabel = module.getDescription() != null 
                    ? module.getName() + "<br/>(" + module.getDescription() + ")"
                    : module.getName();
                
                sb.append("  Project --> ").append(moduleNodeId).append("[\"").append(moduleLabel).append("\"]\n");
                
                if (!module.getTopLevelPackages().isEmpty()) {
                    for (String pkg : module.getTopLevelPackages()) {
                        String pkgNodeId = (moduleNodeId + pkg).replaceAll("[^a-zA-Z0-9]", "");
                        sb.append("  ").append(moduleNodeId).append(" --> ").append(pkgNodeId)
                            .append("[\"").append(pkg).append("\"]\n");
                    }
                }
            }
        }

        return sb.toString();
    }

    private static String generateModuleArchitecture(RepoContext context) {
        // Generate architecture based on actual project structure
        StringBuilder sb = new StringBuilder();
        sb.append("graph TB\n");
        
        // Detect project integrations from context
        boolean hasCopilot = context.isCopilotAvailable();
        boolean hasTests = context.hasTests();
        boolean hasCi = context.hasCi();
        boolean hasDb = context.isHasDatabaseIntegration();
        boolean hasSpring = context.isUsesSpring();
        int depCount = context.getExternalDependencies().size();
        
        // Build layers
        sb.append("  subgraph Entry[\"📋 Entry Point\"]\n");
        sb.append("    Main[\"Main CLI Entry<br/>Argument processing\"]\n");
        sb.append("  end\n\n");
        
        sb.append("  subgraph Analysis[\"🔍 Analysis Layer\"]\n");
        sb.append("    Scanner[\"RepoScanner<br/>Structure analysis\"]\n");
        sb.append("    Metrics[\"RepoMetricsCollector<br/>Metrics collection\"]\n");
        sb.append("    Orch[\"AnalysisOrchestrator<br/>Coordination\"]\n");
        sb.append("  end\n\n");
        
        if (hasCopilot) {
            sb.append("  subgraph AI[\"🤖 AI Layer\"]\n");
            sb.append("    Copilot[\"CopilotClient<br/>GitHub API\"]\n");
            sb.append("  end\n\n");
        }
        
        sb.append("  subgraph Reporting[\"📝 Report Layer\"]\n");
        sb.append("    Findings[\"FindingsGenerator<br/>Analysis results\"]\n");
        sb.append("    Mermaid[\"MermaidGenerator<br/>Diagrams\"]\n");
        sb.append("    MarkdownGen[\"MarkdownReportGenerator<br/>Output formatting\"]\n");
        sb.append("  end\n\n");
        
        sb.append("  subgraph Model[\"💾 Data Model\"]\n");
        sb.append("    RepoContext[\"RepoContext<br/>Analysis results\"]\n");
        if (depCount > 0) {
            sb.append("    Deps[\"External Dependencies<br/>").append(depCount).append(" dependencies\"]\n");
        }
        sb.append("  end\n\n");
        
        if (hasTests || hasDb || hasCi || hasSpring) {
            sb.append("  subgraph Detections[\"🎯 Detected Integrations\"]\n");
            if (hasTests) sb.append("    Tests[\"✓ Test Framework\"]\n");
            if (hasCi) sb.append("    CI[\"✓ CI/CD Pipeline\"]\n");
            if (hasDb) sb.append("    DB[\"✓ Database Integration\"]\n");
            if (hasSpring) sb.append("    Spring[\"✓ Spring Framework\"]\n");
            sb.append("  end\n\n");
        }
        
        // Connections
        sb.append("  Main --> Scanner\n");
        sb.append("  Scanner --> Metrics\n");
        sb.append("  Metrics --> Orch\n");
        if (hasCopilot) {
            sb.append("  Orch --> Copilot\n");
            sb.append("  Copilot --> Findings\n");
        }
        sb.append("  Orch --> Findings\n");
        sb.append("  Findings --> RepoContext\n");
        sb.append("  RepoContext --> Mermaid\n");
        sb.append("  RepoContext --> MarkdownGen\n");
        sb.append("  Mermaid --> MarkdownGen\n");
        if (depCount > 0) {
            sb.append("  RepoContext --> Deps\n");
        }
        
        return sb.toString();
    }
}

