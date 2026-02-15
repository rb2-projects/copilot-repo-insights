package com.rb.repoinsight.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects aggregated repository metrics for AI analysis.
 * Enforces strict limits to control token usage.
 */
public class RepoMetricsCollector {

    private static final int MAX_LARGEST_FILES = 5;
    private static final String[] SOURCE_EXTENSIONS = { ".java", ".kt", ".scala", ".groovy" };

    public RepoMetrics collect(Path repoRoot) {
        RepoMetrics metrics = new RepoMetrics();

        try (Stream<Path> paths = Files.walk(repoRoot)) {
            List<Path> sourceFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSourceFile)
                    .filter(p -> !p.toString().contains("target")) // Exclude build outputs
                    .filter(p -> !p.toString().contains("build"))
                    .collect(Collectors.toList());

            metrics.setTotalFiles(sourceFiles.size());

            // Count classes and test classes
            long classCount = sourceFiles.stream()
                    .filter(p -> !isTestFile(p))
                    .count();
            long testClassCount = sourceFiles.stream()
                    .filter(this::isTestFile)
                    .count();

            metrics.setTotalClasses((int) classCount);
            metrics.setTotalTestClasses((int) testClassCount);

            // Calculate approximate LOC
            long totalLoc = sourceFiles.stream()
                    .mapToLong(this::countLines)
                    .sum();
            metrics.setApproximateLinesOfCode(totalLoc);

            // Identify largest files (top 5)
            List<RepoMetrics.FileMetric> largestFiles = sourceFiles.stream()
                    .map(p -> new RepoMetrics.FileMetric(
                            repoRoot.relativize(p).toString(),
                            (int) countLines(p)))
                    .sorted(Comparator.comparingInt(RepoMetrics.FileMetric::getLineCount).reversed())
                    .limit(MAX_LARGEST_FILES)
                    .collect(Collectors.toList());
            metrics.setLargestFiles(largestFiles);

            // Extract top-level packages
            List<String> topLevelPackages = sourceFiles.stream()
                    .map(p -> extractTopLevelPackage(repoRoot, p))
                    .filter(pkg -> pkg != null && !pkg.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            metrics.setTopLevelPackages(topLevelPackages);

        } catch (IOException e) {
            // Best-effort: return partial metrics
            System.err.println("Warning: Failed to collect complete metrics: " + e.getMessage());
        }

        return metrics;
    }

    private boolean isSourceFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : SOURCE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTestFile(Path path) {
        String pathStr = path.toString();
        return pathStr.contains("src/test/") ||
                pathStr.contains("src\\test\\") ||
                path.getFileName().toString().contains("Test");
    }

    private long countLines(Path path) {
        try {
            return Files.lines(path).count();
        } catch (IOException e) {
            return 0;
        }
    }

    private String extractTopLevelPackage(Path repoRoot, Path sourceFile) {
        Path relativePath = repoRoot.relativize(sourceFile);
        String pathStr = relativePath.toString();

        // Extract package from path like src/main/java/com/example/...
        if (pathStr.contains("src/main/java/") || pathStr.contains("src\\main\\java\\")) {
            String packagePath = pathStr.substring(pathStr.indexOf("java") + 5);
            int firstSeparator = Math.min(
                    packagePath.indexOf('/') != -1 ? packagePath.indexOf('/') : Integer.MAX_VALUE,
                    packagePath.indexOf('\\') != -1 ? packagePath.indexOf('\\') : Integer.MAX_VALUE);
            if (firstSeparator != Integer.MAX_VALUE) {
                return packagePath.substring(0, firstSeparator);
            }
        }
        return null;
    }
}
