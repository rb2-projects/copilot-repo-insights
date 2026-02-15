package com.rb.repoinsight.report;

import com.rb.repoinsight.model.DependencyCategory;
import com.rb.repoinsight.model.RepoContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a deterministic "Findings & Recommendations" section based on
 * RepoContext signals.
 */
public class FindingsGenerator {

    public static String generate(RepoContext context) {
        List<String> hygieneFindings = collectHygieneFindings(context);
        List<String> integrationFindings = collectIntegrationFindings(context);
        List<String> recommendations = collectRecommendations(context);

        StringBuilder sb = new StringBuilder();

        sb.append("## Findings & Recommendations\n\n");

        // 1. Summary
        sb.append("### Summary\n");
        sb.append(
                String.format("Repository analysis identified %d hygiene observations and %d integration patterns.\n\n",
                        hygieneFindings.size(), integrationFindings.size()));

        // 2. Findings (Always shown)
        sb.append("### Findings\n");
        for (String finding : hygieneFindings) {
            sb.append("- ").append(finding).append("\n");
        }
        for (String finding : integrationFindings) {
            sb.append("- ").append(finding).append("\n");
        }
        sb.append("\n");

        // 3. Recommendations (Shown only if gaps exist, Max 4)
        if (!recommendations.isEmpty()) {
            sb.append("### Recommendations\n");
            int count = 0;
            for (String rec : recommendations) {
                if (count >= 4)
                    break;
                sb.append("- ").append(rec).append("\n");
                count++;
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static List<String> collectHygieneFindings(RepoContext context) {
        List<String> findings = new ArrayList<>();

        if (context.hasTests()) {
            findings.add("Test sources are present in the repository.");
        } else {
            findings.add("No test sources were detected.");
        }

        if (context.hasCi()) {
            findings.add("CI configuration was detected.");
        } else {
            findings.add("No CI configuration was detected.");
        }

        return findings;
    }

    public static List<String> collectIntegrationFindings(RepoContext context) {
        List<String> findings = new ArrayList<>();

        if (hasCategory(context, DependencyCategory.PERSISTENCE)) {
            findings.add("The project integrates with external persistence systems.");
        }
        if (hasCategory(context, DependencyCategory.MESSAGING)) {
            findings.add("The project integrates with external systems via messaging.");
        }
        if (hasCategory(context, DependencyCategory.CLOUD_SERVICES)) {
            findings.add("The project integrates with various cloud provider services.");
        }
        if (hasCategory(context, DependencyCategory.WEB)) {
            findings.add("The project integrates with external systems via HTTP APIs.");
        }

        return findings;
    }

    public static List<String> collectRecommendations(RepoContext context) {
        List<String> recs = new ArrayList<>();

        // Test/CI Variants
        if (context.hasTests() && !context.hasCi()) {
            recs.add("Consider adding a CI pipeline to automatically run tests on pull requests.");
        } else if (context.hasCi() && !context.hasTests()) {
            recs.add("Consider adding test sources to leverage the existing CI pipeline.");
        } else if (!context.hasTests() && !context.hasCi()) {
            recs.add("Consider implementing a test suite to ensure code correctness and prevent regressions.");
        }

        // Integration Recommendations
        if (hasCategory(context, DependencyCategory.PERSISTENCE)) {
            recs.add("Consider using an in-memory database or containers for local/CI persistence testing.");
        }
        if (hasCategory(context, DependencyCategory.MESSAGING)) {
            recs.add("Consider establishing monitoring and failure-handling strategies for message consumption.");
        }
        if (hasCategory(context, DependencyCategory.CLOUD_SERVICES)) {
            recs.add("Consider using LocalStack or similar emulators for validating cloud service integrations.");
        }
        if (hasCategory(context, DependencyCategory.WEB)) {
            recs.add("Consider using stubs or consumer-driven contract tests for external HTTP integrations.");
        }

        return recs;
    }

    private static boolean hasCategory(RepoContext context, DependencyCategory category) {
        return context.getExternalDependencies().stream()
                .anyMatch(d -> category.equals(d.getCategoryEnum()));
    }
}
