package com.rb.repoinsight.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.rb.repoinsight.constants.PromptsConfig;
import com.rb.repoinsight.constants.RepoConstants;
import com.rb.repoinsight.model.RepoContext;
import com.rb.repoinsight.scan.RuleEngineScanner;

public class RepoScanner {

    public RepoContext scan(Path repoRoot) {
        RepoContext context = new RepoContext();
        context.setRepoPath(repoRoot.toAbsolutePath().toString());

        RuleEngineScanner ruleEngineScanner = new RuleEngineScanner();

        List<com.rb.repoinsight.model.ExternalDependency> allDeps = new ArrayList<>(ruleEngineScanner.scan(repoRoot));

        context.setExternalDependencies(allDeps);

        detectBuildTool(repoRoot, context);
        detectTests(repoRoot, context);
        detectCi(repoRoot, context);

        if (context.getBuildTool() != null) {
            detectPackagingAndFrameworks(repoRoot, context);
        }

        detectLanguage(repoRoot, context);

        try {
            Process process = new ProcessBuilder(
                    "copilot", "suggest",
                    "--type", "chat",
                    PromptsConfig.ARCHITECTURAL_OVERVIEW_PROMPT)
                    .directory(repoRoot.toFile())
                    .redirectErrorStream(true)
                    .start();

            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();

            if (exitCode == 0 && !output.isBlank()) {
                context.setCopilotAvailable(true);
                context.setCopilotOutput(output.trim());
            } else {
                context.setCopilotAvailable(false);
                context.setCopilotFailureReason(
                        "Copilot CLI returned no output (exit code " + exitCode + ")");
            }
        } catch (Exception e) {
            context.setCopilotAvailable(false);
            context.setCopilotFailureReason(e.getMessage());
        }

        return context;
    }

    // --------------------------------------------------
    // Detection methods
    // --------------------------------------------------

    private void detectLanguage(Path repoRoot, RepoContext context) {
        // Check standard locations first
        if (Files.exists(repoRoot.resolve("src/main/java"))) {
            context.setLanguage(RepoConstants.LANGUAGE_JAVA);
            return;
        } else if (Files.exists(repoRoot.resolve("src/main/kotlin"))) {
            context.setLanguage(RepoConstants.LANGUAGE_KOTLIN);
            return;
        }
        
        // For Maven projects, try to parse pom.xml for custom sourceDirectory
        if (RepoConstants.BUILD_TOOL_MAVEN.equals(context.getBuildTool())) {
            try {
                String pomContent = Files.readString(repoRoot.resolve("pom.xml"));
                
                // Look for custom sourceDirectory in pom.xml
                if (pomContent.contains("<sourceDirectory>")) {
                    int start = pomContent.indexOf("<sourceDirectory>") + "<sourceDirectory>".length();
                    int end = pomContent.indexOf("</sourceDirectory>", start);
                    if (start < end) {
                        String sourceDir = pomContent.substring(start, end).trim();
                        Path customSource = repoRoot.resolve(sourceDir);
                        if (Files.exists(customSource)) {
                            if (hasJavaFiles(customSource)) {
                                context.setLanguage(RepoConstants.LANGUAGE_JAVA);
                                return;
                            } else if (hasKotlinFiles(customSource)) {
                                context.setLanguage(RepoConstants.LANGUAGE_KOTLIN);
                                return;
                            }
                        }
                    }
                }
                
                // If pom.xml is explicitly Java and no source files found, still mark as Java
                if (pomContent.contains("java")) {
                    context.setLanguage(RepoConstants.LANGUAGE_JAVA);
                    return;
                }
            } catch (IOException e) {
                // Continue with directory-based detection
            }
        }
        
        // Check for any Java files in the repository
        if (hasJavaFilesAnywhere(repoRoot)) {
            context.setLanguage(RepoConstants.LANGUAGE_JAVA);
        } else if (hasKotlinFilesAnywhere(repoRoot)) {
            context.setLanguage(RepoConstants.LANGUAGE_KOTLIN);
        } else {
            context.setLanguage(RepoConstants.LANGUAGE_UNKNOWN);
        }
    }
    
    private boolean hasJavaFiles(Path dir) {
        try (var stream = Files.walk(dir)) {
            return stream.anyMatch(p -> p.toString().endsWith(".java"));
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean hasKotlinFiles(Path dir) {
        try (var stream = Files.walk(dir)) {
            return stream.anyMatch(p -> p.toString().endsWith(".kt"));
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean hasJavaFilesAnywhere(Path root) {
        try (var stream = Files.walk(root)) {
            return stream.limit(1000).anyMatch(p -> p.toString().endsWith(".java"));
        } catch (IOException e) {
            return false;
        }
    }
    
    private boolean hasKotlinFilesAnywhere(Path root) {
        try (var stream = Files.walk(root)) {
            return stream.limit(1000).anyMatch(p -> p.toString().endsWith(".kt"));
        } catch (IOException e) {
            return false;
        }
    }

    private void detectBuildTool(Path repoRoot, RepoContext context) {
        if (Files.exists(repoRoot.resolve("pom.xml"))) {
            context.setBuildTool(RepoConstants.BUILD_TOOL_MAVEN);
        } else if (Files.exists(repoRoot.resolve("build.gradle"))
                || Files.exists(repoRoot.resolve("build.gradle.kts"))) {
            context.setBuildTool(RepoConstants.BUILD_TOOL_GRADLE);
        }
    }

    private void detectTests(Path repoRoot, RepoContext context) {
        try {
            Path testDir = repoRoot.resolve("src/test");
            if (!Files.exists(testDir)) {
                context.setHasTests(false);
                return;
            }
            
            // Check if there are actual test files (not just empty directory)
            try (var stream = Files.walk(testDir)) {
                boolean hasTestFiles = stream
                        .filter(Files::isRegularFile)
                        .anyMatch(path -> path.toString().endsWith(".java"));
                context.setHasTests(hasTestFiles);
            }
        } catch (Exception e) {
            context.setHasTests(false);
        }
    }

    private void detectCi(Path repoRoot, RepoContext context) {
        context.setHasCi(
                Files.exists(repoRoot.resolve(".github/workflows"))
                        || Files.exists(repoRoot.resolve(".gitlab-ci.yml")));
    }

    private void detectPackagingAndFrameworks(Path repoRoot, RepoContext context) {
        try {
            String buildFilesContent = readBuildFiles(repoRoot, context.getBuildTool());

            detectPackaging(buildFilesContent, context);
            detectSpring(buildFilesContent, context);
            detectDatabaseIntegration(buildFilesContent, context);

        } catch (IOException e) {
            // Best-effort scan; do not fail the whole analysis
            context.setPackagingType(RepoConstants.PACKAGING_UNKNOWN);
        }
    }

    // --------------------------------------------------
    // Heuristic helpers
    // --------------------------------------------------

    private String readBuildFiles(Path repoRoot, String buildTool) throws IOException {
        StringBuilder content = new StringBuilder();

        if (RepoConstants.BUILD_TOOL_MAVEN.equals(buildTool)) {
            content.append(Files.readString(repoRoot.resolve("pom.xml")));
        }

        if (RepoConstants.BUILD_TOOL_GRADLE.equals(buildTool)) {
            Path gradle = repoRoot.resolve("build.gradle");
            Path gradleKts = repoRoot.resolve("build.gradle.kts");

            if (Files.exists(gradle)) {
                content.append(Files.readString(gradle));
            }
            if (Files.exists(gradleKts)) {
                content.append(Files.readString(gradleKts));
            }
        }

        return content.toString();
    }

    private void detectPackaging(String buildFilesContent, RepoContext context) {
        // Try to parse <packaging> element from pom.xml
        if (buildFilesContent.contains("<packaging>")) {
            int start = buildFilesContent.indexOf("<packaging>") + "<packaging>".length();
            int end = buildFilesContent.indexOf("</packaging>", start);
            if (start < end) {
                String packaging = buildFilesContent.substring(start, end).trim();
                if ("war".equalsIgnoreCase(packaging)) {
                    context.setPackagingType(RepoConstants.PACKAGING_WAR);
                    return;
                } else if ("ear".equalsIgnoreCase(packaging) || "jar".equalsIgnoreCase(packaging)) {
                    context.setPackagingType(RepoConstants.PACKAGING_JAR);
                    return;
                }
            }
        }
        
        // Fallback: detect based on presence of WAR indicators
        if (buildFilesContent.contains("<packaging>war</packaging>")
                || (buildFilesContent.contains("war") && buildFilesContent.contains("webapp"))) {
            context.setPackagingType(RepoConstants.PACKAGING_WAR);
        } else {
            // Default to JAR (Maven convention)
            context.setPackagingType(RepoConstants.PACKAGING_JAR);
        }
    }

    private void detectSpring(String buildFilesContent, RepoContext context) {
        boolean springDetected = RepoConstants.SPRING_IDENTIFIERS
                .stream()
                .anyMatch(buildFilesContent::contains);

        context.setUsesSpring(springDetected);
    }

    private void detectDatabaseIntegration(String buildFilesContent, RepoContext context) {
        boolean dbDetected = RepoConstants.DATABASE_IDENTIFIERS
                .stream()
                .anyMatch(buildFilesContent::contains);

        context.setHasDatabaseIntegration(dbDetected);
    }
}
