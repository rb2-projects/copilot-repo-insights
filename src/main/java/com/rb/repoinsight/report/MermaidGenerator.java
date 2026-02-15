package com.rb.repoinsight.report;

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
}

