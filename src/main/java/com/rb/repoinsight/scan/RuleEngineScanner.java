package com.rb.repoinsight.scan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rb.repoinsight.model.ExternalDependency;
import com.rb.repoinsight.model.RuleConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleEngineScanner {

    private final List<RuleConfig> rules;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuleEngineScanner() {
        this.rules = loadRules();
    }

    private List<RuleConfig> loadRules() {
        try (InputStream is = getClass().getResourceAsStream("/rules.json")) {
            if (is == null) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(is, new TypeReference<List<RuleConfig>>() {
            });
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public List<ExternalDependency> scan(Path repoRoot) {
        Set<String> artifacts = extractMavenArtifacts(repoRoot);
        Set<String> detectedSystemNames = new HashSet<>();
        List<ExternalDependency> results = new ArrayList<>();

        // 1. Library-based detection
        for (RuleConfig rule : rules) {
            for (String artifact : rule.getMavenArtifacts()) {
                if (artifacts.contains(artifact)) {
                    if (detectedSystemNames.add(rule.getName() + rule.getCategory().name())) {
                        results.add(new ExternalDependency(rule.getName(), rule.getCategory(),
                                "Library: " + artifact));
                    }
                }
            }
        }

        // 2. Heuristic-based detection
        try (Stream<Path> paths = Files.walk(repoRoot)) {
            List<Path> filesToScan = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isInterestingFile)
                    .collect(Collectors.toList());

            for (Path file : filesToScan) {
                try {
                    String content = Files.readString(file);
                    for (RuleConfig rule : rules) {
                        for (String regex : rule.getHeuristics()) {
                            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            Matcher m = p.matcher(content);
                            if (m.find()) {
                                if (detectedSystemNames.add(rule.getName() + rule.getCategory().name())) {
                                    String evidence = "Found in " + repoRoot.relativize(file) + ": " + m.group().trim();
                                    results.add(new ExternalDependency(rule.getName(), rule.getCategory(), evidence));
                                }
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

        return results;
    }

    private Set<String> extractMavenArtifacts(Path repoRoot) {
        Set<String> artifacts = new HashSet<>();
        Path pom = repoRoot.resolve("pom.xml");
        if (Files.exists(pom)) {
            try {
                String content = Files.readString(pom);
                // Simple regex to find artifactIds inside dependencies
                // This is faster than full XML parsing and sufficient for heuristics
                Pattern p = Pattern.compile("<artifactId>(.*?)</artifactId>");
                Matcher m = p.matcher(content);
                while (m.find()) {
                    artifacts.add(m.group(1).trim());
                }
            } catch (IOException ignored) {
            }
        }
        return artifacts;
    }

    private boolean isInterestingFile(Path path) {
        String pathString = path.toString().replace("\\", "/");
        // Don't scan our own source to avoid false positives by rules.json itself or
        // this scanner
        if (pathString.contains("com/rb/repoinsight") || pathString.contains("rules.json")) {
            return false;
        }

        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".java") ||
                name.endsWith(".xml") ||
                name.endsWith(".yml") ||
                name.endsWith(".yaml") ||
                name.endsWith(".properties") ||
                name.endsWith(".json");
    }
}
