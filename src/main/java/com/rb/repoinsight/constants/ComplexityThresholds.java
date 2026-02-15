package com.rb.repoinsight.constants;

/**
 * Configurable thresholds for code quality analysis.
 * Adjust these values to customize complexity signal detection.
 */
public class ComplexityThresholds {

    /**
     * Files with more lines than this are flagged as "large files"
     */
    public static final int LARGE_FILE_LINES = 300;

    /**
     * Methods with more lines than this are flagged as "long methods"
     */
    public static final int LONG_METHOD_LINES = 40;

    /**
     * Classes with more lines than this are flagged as "large classes"
     */
    public static final int LARGE_CLASS_LINES = 40;

    /**
     * Test coverage thresholds for color coding:
     * - GREEN: > 80%
     * - YELLOW: 50% to 80%
     * - RED: < 50%
     */
    public static final int TEST_COVERAGE_GREEN = 80;
    public static final int TEST_COVERAGE_YELLOW_MIN = 50;

    private ComplexityThresholds() {
        // Utility class
    }
}
