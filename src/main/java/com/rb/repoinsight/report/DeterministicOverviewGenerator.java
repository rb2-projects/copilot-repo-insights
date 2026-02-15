package com.rb.repoinsight.report;

import com.rb.repoinsight.model.ExternalDependency;
import com.rb.repoinsight.model.RepoContext;

import java.util.stream.Collectors;

/**
 * Generates a deterministic overview paragraph based on RepoContext.
 */
public class DeterministicOverviewGenerator {

    public static String generate(RepoContext context) {
        StringBuilder sb = new StringBuilder();

        // 1. Language and Build Tool
        sb.append("This is a **").append(context.getLanguage()).append("** project ");
        if (context.getBuildTool() != null && !context.getBuildTool().equals("Unknown")) {
            sb.append("managed by **").append(context.getBuildTool()).append("**. ");
        } else {
            sb.append("with no detected build tool. ");
        }

        // 2. Framework and Packaging
        if (context.isUsesSpring()) {
            sb.append("It uses the **Spring** framework. ");
        }
        if (context.getPackagingType() != null && !context.getPackagingType().isBlank()) {
            sb.append("The codebase is packaged as a **").append(context.getPackagingType()).append("**. ");
        }

        // 3. External Dependencies
        if (!context.getExternalDependencies().isEmpty()) {
            var categories = context.getExternalDependencies().stream()
                    .map(ExternalDependency::getCategory)
                    .distinct()
                    .collect(Collectors.joining(", "));

            sb.append("Deterministic analysis indicates interactions with **")
                    .append(context.getExternalDependencies().size())
                    .append("** external system(s), primarily for **")
                    .append(categories)
                    .append("**. ");
        }

        // 4. Quality and CI
        if (context.hasTests()) {
            sb.append("The repository contains **test sources**. ");
        }
        if (context.hasCi()) {
            sb.append("CI configuration is **available** for this project. ");
        }

        return sb.toString().trim();
    }
}
