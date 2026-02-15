package com.rb.repoinsight.scan;

import java.util.ArrayList;
import java.util.List;

import com.rb.repoinsight.constants.ComplexityThresholds;
import com.rb.repoinsight.model.RepoContext;

/**
 * Analyzes code complexity signals and maintainability concerns.
 */
public class ComplexityAnalyzer {

    /**
     * Analyze the repository for complexity signals and maintainability concerns.
     * 
     * @param context Repository context with metrics
     * @param metrics Repository metrics
     */
    public static void analyze(RepoContext context, RepoMetrics metrics) {
        List<String> signals = new ArrayList<>();
        List<String> concerns = new ArrayList<>();

        // Check for large files with specifics
        List<String> largeFileDetails = new ArrayList<>();
        for (RepoMetrics.FileMetric file : metrics.getLargestFiles()) {
            if (file.getLineCount() > ComplexityThresholds.LARGE_FILE_LINES) {
                largeFileDetails.add(getFileName(file.getRelativePath()) + " (" + file.getLineCount() + " LOC)");
            }
        }
        if (!largeFileDetails.isEmpty()) {
            signals.add("Large file" + (largeFileDetails.size() > 1 ? "s" : "") + " (>" + 
                ComplexityThresholds.LARGE_FILE_LINES + " LOC): " + String.join(", ", largeFileDetails));
        }

        // Check test coverage concern
        int coverage = context.getTestCoveragePercentage();
        if (coverage < 50) {
            concerns.add("Low test coverage (" + coverage + "%) - Below 50% threshold");
        } else if (coverage < 80) {
            concerns.add("Moderate test coverage (" + coverage + "%) - Below optimal 80% threshold");
        }

        // Check for untested large files
        if (!context.hasTests()) {
            // If no tests at all, flag this
            signals.add("No test coverage detected");
            concerns.add("No test suite present - Critical for maintainability");
        } else {
            // Check if there are large files that might not be tested
            List<String> unterstedLargeDetails = new ArrayList<>();
            for (RepoMetrics.FileMetric file : metrics.getLargestFiles()) {
                if (file.getLineCount() > ComplexityThresholds.LARGE_FILE_LINES) {
                    unterstedLargeDetails.add(getFileName(file.getRelativePath()));
                }
            }
            if (!unterstedLargeDetails.isEmpty()) {
                signals.add("Large untested file" + (unterstedLargeDetails.size() > 1 ? "s" : "") + ": " + 
                    String.join(", ", unterstedLargeDetails));
            }
        }

        // High class/file ratio suggests complexity
        if (metrics.getTotalClasses() > 50) {
            concerns.add("Large codebase (" + metrics.getTotalClasses() + " classes) - Consider modularization");
        }

        context.setComplexitySignals(signals);
        context.setMaintainabilityConcerns(concerns);
    }

    /**
     * Extract just the filename from a full path
     */
    private static String getFileName(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "unknown";
        }
        int lastSlash = Math.max(relativePath.lastIndexOf('/'), relativePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? relativePath.substring(lastSlash + 1) : relativePath;
    }
}
