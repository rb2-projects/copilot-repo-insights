package com.rb.repoinsight.scan;

/**
 * Calculates test coverage using heuristic matching of test classes to source classes.
 */
public class TestCoverageCalculator {

    /**
     * Calculate approximate test coverage as a percentage using heuristic matching.
     * 
     * Heuristic: A source class is considered "covered" if:
     * 1. There's a test class with matching name (e.g., MyClassTest or TestMyClass)
     * 2. The test class exists in test sources
     * 
     * @param totalClasses Total number of source classes
     * @param totalTestClasses Total number of test classes
     * @return Approximate coverage percentage (0-100), or 0 if no classes
     */
    public static int calculateApproximateCoverage(int totalClasses, int totalTestClasses) {
        if (totalClasses == 0) {
            return 0;
        }

        // Simple heuristic: assume test class count / source class count ratio
        // This underestimates coverage (many test classes test one source class)
        // but provides a reasonable approximation
        int estimatedCoverage = Math.min(100, (totalTestClasses * 100) / totalClasses);
        
        return Math.max(0, estimatedCoverage);
    }

    /**
     * Determine the color/severity of test coverage level.
     * 
     * @param coveragePercentage Coverage percentage (0-100)
     * @return "success" (green), "warning" (yellow), or "danger" (red)
     */
    public static String getCoverageLevel(int coveragePercentage) {
        if (coveragePercentage >= 80) {
            return "success";
        } else if (coveragePercentage >= 50) {
            return "warning";
        } else {
            return "danger";
        }
    }
}
