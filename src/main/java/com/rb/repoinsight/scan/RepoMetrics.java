package com.rb.repoinsight.scan;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated repository metrics for AI analysis.
 * Contains only metadata - NO source code.
 */
public class RepoMetrics {

    private int totalFiles;
    private int totalClasses;
    private int totalTestClasses;
    private long approximateLinesOfCode;
    private List<String> topLevelPackages = new ArrayList<>();
    private List<FileMetric> largestFiles = new ArrayList<>();

    public static class FileMetric {
        private final String relativePath;
        private final int lineCount;

        public FileMetric(String relativePath, int lineCount) {
            this.relativePath = relativePath;
            this.lineCount = lineCount;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public int getLineCount() {
            return lineCount;
        }
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getTotalTestClasses() {
        return totalTestClasses;
    }

    public void setTotalTestClasses(int totalTestClasses) {
        this.totalTestClasses = totalTestClasses;
    }

    public long getApproximateLinesOfCode() {
        return approximateLinesOfCode;
    }

    public void setApproximateLinesOfCode(long approximateLinesOfCode) {
        this.approximateLinesOfCode = approximateLinesOfCode;
    }

    public List<String> getTopLevelPackages() {
        return topLevelPackages;
    }

    public void setTopLevelPackages(List<String> topLevelPackages) {
        this.topLevelPackages = topLevelPackages;
    }

    public List<FileMetric> getLargestFiles() {
        return largestFiles;
    }

    public void setLargestFiles(List<FileMetric> largestFiles) {
        this.largestFiles = largestFiles;
    }
}
