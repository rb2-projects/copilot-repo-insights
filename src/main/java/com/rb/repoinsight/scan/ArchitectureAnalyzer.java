package com.rb.repoinsight.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rb.repoinsight.model.ProjectModule;
import com.rb.repoinsight.model.RepoContext;

/**
 * Analyzes project architecture by extracting modules and packages.
 * Can optionally enhance with AI descriptions if Copilot is available.
 */
public class ArchitectureAnalyzer {

    /**
     * Analyze the target project's architecture structure.
     * Extracts modules (from pom.xml) and top-level packages from source directories.
     */
    public static void analyze(Path repoRoot, RepoContext context) {
        List<ProjectModule> modules = new ArrayList<>();

        // 1. Extract modules from pom.xml (if multi-module project)
        List<String> pomModules = extractModulesFromPom(repoRoot);

        if (!pomModules.isEmpty()) {
            // Multi-module Maven project
            for (String moduleName : pomModules) {
                Path modulePath = repoRoot.resolve(moduleName);
                ProjectModule module = new ProjectModule(moduleName, moduleName);
                
                // Extract packages from this module
                List<String> packages = extractTopLevelPackages(modulePath);
                module.setTopLevelPackages(packages);
                modules.add(module);
            }
        } else {
            // Single module project - treat root as the module
            String projectName = repoRoot.getFileName().toString();
            ProjectModule rootModule = new ProjectModule(projectName, ".");
            
            List<String> packages = extractTopLevelPackages(repoRoot);
            rootModule.setTopLevelPackages(packages);
            modules.add(rootModule);
        }

        context.setProjectModules(modules);
    }

    /**
     * Extract module names from pom.xml <modules> section.
     */
    private static List<String> extractModulesFromPom(Path repoRoot) {
        try {
            Path pomFile = repoRoot.resolve("pom.xml");
            if (!Files.exists(pomFile)) {
                return Collections.emptyList();
            }

            String pomContent = Files.readString(pomFile);
            List<String> modules = new ArrayList<>();

            // Regex to match <modules> ... </modules> section
            Pattern modulesPattern = Pattern.compile(
                    "<modules>\\s*(.*?)\\s*</modules>",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher modulesMatcher = modulesPattern.matcher(pomContent);

            if (modulesMatcher.find()) {
                String modulesSection = modulesMatcher.group(1);
                
                // Extract individual <module> entries
                Pattern modulePattern = Pattern.compile(
                        "<module>\\s*([^<]+)\\s*</module>");
                Matcher moduleMatcher = modulePattern.matcher(modulesSection);

                while (moduleMatcher.find()) {
                    String moduleName = moduleMatcher.group(1).trim();
                    modules.add(moduleName);
                }
            }

            return modules;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Extract top-level packages from src/main/java directory.
     * Looks for package names by examining directory structure and Java files.
     * Handles both standard Maven layout and aggregator POMs.
     */
    private static List<String> extractTopLevelPackages(Path modulePath) {
        Set<String> packages = new TreeSet<>();

        // Try standard Maven location
        Path srcDir = modulePath.resolve("src/main/java");
        if (Files.exists(srcDir)) {
            try (var stream = Files.list(srcDir)) {
                stream.filter(Files::isDirectory)
                        .map(p -> p.getFileName().toString())
                        .forEach(packages::add);
            } catch (IOException ignored) {
            }
        }

        // If no packages found and this looks like an aggregator POM, 
        // check subdirectories for src/main/java
        if (packages.isEmpty()) {
            try (var stream = Files.list(modulePath)) {
                stream.filter(Files::isDirectory)
                        .filter(p -> !p.getFileName().toString().startsWith("."))
                        .forEach(subdir -> {
                            Path subSrcDir = subdir.resolve("src/main/java");
                            if (Files.exists(subSrcDir)) {
                                try (var subStream = Files.list(subSrcDir)) {
                                    subStream.filter(Files::isDirectory)
                                            .map(p -> p.getFileName().toString())
                                            .forEach(packages::add);
                                } catch (IOException ignored) {
                                }
                            }
                        });
            } catch (IOException ignored) {
            }
        }

        // Also try custom sourceDirectory from pom.xml
        try {
            Path pomFile = modulePath.resolve("pom.xml");
            if (Files.exists(pomFile)) {
                String pomContent = Files.readString(pomFile);
                
                // Extract custom sourceDirectory
                Pattern pattern = Pattern.compile(
                        "<sourceDirectory>\\s*([^<]+)\\s*</sourceDirectory>");
                Matcher matcher = pattern.matcher(pomContent);
                
                if (matcher.find()) {
                    String sourceDir = matcher.group(1).trim();
                    Path customSrc = modulePath.resolve(sourceDir);
                    
                    if (Files.exists(customSrc)) {
                        try (var stream = Files.list(customSrc)) {
                            stream.filter(Files::isDirectory)
                                    .map(p -> p.getFileName().toString())
                                    .forEach(packages::add);
                        } catch (IOException ignored) {
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }

        return new ArrayList<>(packages);
    }

    /**
     * Enhance modules with AI descriptions using Copilot.
     * Generates a prompt asking Copilot to describe each module's purpose.
     */
    public static void enhanceWithAiDescriptions(Path repoRoot, RepoContext context) {
        if (!context.isCopilotAvailable() || context.getProjectModules().isEmpty()) {
            return;
        }

        try {
            // Use existing Copilot output if available, or make a new request
            String output = context.getCopilotOutput();
            
            if (output != null && !output.isBlank()) {
                // Parse descriptions from Copilot output
                String[] lines = output.split("\n");
                Map<String, String> descriptions = new HashMap<>();
                
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String moduleName = parts[0].trim();
                            String description = parts[1].trim();
                            descriptions.put(moduleName, description);
                        }
                    }
                }

                // Apply descriptions to modules
                for (ProjectModule module : context.getProjectModules()) {
                    String description = descriptions.get(module.getName());
                    if (description != null) {
                        module.setDescription(description);
                    }
                }
            }
        } catch (Exception e) {
            // Best-effort only, don't fail the analysis
        }
    }
}
