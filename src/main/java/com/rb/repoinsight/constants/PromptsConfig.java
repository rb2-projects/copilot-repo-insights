package com.rb.repoinsight.constants;

import com.rb.repoinsight.model.RepoContext;

/**
 * Centralized configuration for all AI prompts used in Repo Insight.
 * This file maintains version control history of all prompt engineering decisions.
 * All prompts are kept here for transparency, easy auditing, and consistent tuning.
 */
public class PromptsConfig {

    /**
     * Prompt for generating a short architectural overview via copilot CLI.
     * Used when scanning the repository structure.
     */
    public static final String ARCHITECTURAL_OVERVIEW_PROMPT = 
        "Provide a technical project overview of this repository in 3-4 sentences. Focus on: (1) project type and purpose, (2) architectural style, (3) technology stack, (4) maturity level. Be direct and avoid showing your analysis process. Output only the final overview, no explanations or debug steps.";


    /**
     * Prompt for generating a concise project overview based on detected facts.
     * Variables: buildTool, language, hasTests, hasCi, usesSpring, hasDatabaseIntegration
     */
    public static String buildProjectOverviewPrompt(RepoContext context) {
        return """
                Analyze this repository and write a 2-3 sentence project overview. Be factual and specific.

                Repository Facts:
                Build tool: %s
                Language: %s
                Tests present: %s
                CI/CD configured: %s
                Uses Spring Framework: %s
                Database integration: %s

                Write a description that:
                1. Identifies the project type (CLI tool, library, web application, etc.)
                2. States its primary purpose based on structure
                3. Notes key technology choices (Maven/Gradle, test presence, CI status)

                Keep it to exactly 2-3 sentences. Be direct and technical.

                Overview:
                """.formatted(
                context.getBuildTool(),
                context.getLanguage(),
                context.hasTests(),
                context.hasCi(),
                context.isUsesSpring(),
                context.isHasDatabaseIntegration());
    }

    /**
     * Comprehensive analysis prompt for repository assessment.
     * Used to generate detailed architectural analysis with risk assessment.
     * Template variables: projectName, buildTool, language, packaging, totalFiles,
     * totalClasses, totalTestClasses, approximateLinesOfCode, topLevelPackages,
     * largestFiles, frameworks, externalDependencies, testsPresent, ciPresent
     */
    public static final String COMPREHENSIVE_ANALYSIS_TEMPLATE = """
            Analyze this Java repository comprehensively. Provide specific, measurable assessments based on the facts provided.

            === REPOSITORY FACTS ===
            Project: {{projectName}}
            Build System: {{buildTool}}
            Language: {{language}}
            Total Files: {{totalFiles}} | Classes: {{totalClasses}} | Test Classes: {{totalTestClasses}} | Approximate LOC: {{approximateLinesOfCode}}
            Package Structure: {{topLevelPackages}}
            Largest Files: {{largestFiles}}
            Frameworks: {{frameworks}}
            External Dependencies: {{externalDependencies}}
            Testing Status: {{testsPresent}}
            CI/CD Status: {{ciPresent}}

            === ANALYSIS REQUIRED ===
            
            1. ARCHITECTURAL STYLE (1-2 sentences)
            Identify the dominant architectural pattern (e.g., modular, layered, service-oriented, plugin-based). Base this on package structure and size distribution.

            2. KEY RISKS (2-3 specific risks with brief explanation each)
            Evaluate risks in these categories: maintainability (codebase complexity), test coverage ({{totalTestClasses}} test classes for {{totalClasses}} classes), deployment readiness (CI status: {{ciPresent}}), and dependency management.

            3. MAINTAINABILITY ASSESSMENT (1 sentence with rating)
            Rate as Good/Fair/Poor based on: test coverage ratio, file size distribution, and package organization. Reference specific numbers.

            4. PRODUCTION READINESS (1-2 sentences)
            Assess based on: test coverage, CI/CD presence ({{ciPresent}}), and codebase maturity. Identify blocking issues if any.

            5. THREE SPECIFIC RECOMMENDATIONS
            Provide actionable improvements for this specific project type. Reference actual metrics or package names where possible.

            === OUTPUT FORMAT ===
            Respond with these five sections clearly labeled. Be specific, reference the data provided, use the actual numbers and names from this repository.
            """;

}
