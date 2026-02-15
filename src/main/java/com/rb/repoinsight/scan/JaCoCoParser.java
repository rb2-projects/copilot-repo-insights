package com.rb.repoinsight.scan;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parses JaCoCo coverage reports to extract accurate test coverage percentages.
 * 
 * JaCoCo generates CSV reports that can be parsed to get instruction/line coverage.
 */
public class JaCoCoParser {

    /**
     * Parse JaCoCo coverage report and extract overall coverage percentage.
     * 
     * JaCoCo creates a CSV file at: target/site/jacoco/jacoco.csv
     * Format: GROUP,PACKAGE,CLASS,SOURCEFILE,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
     * 
     * @param repoRoot Repository root directory
     * @return Coverage percentage (0-100), or -1 if JaCoCo report not found
     */
    public static int parseJaCoCoReport(Path repoRoot) {
        Path jacocoReport = repoRoot.resolve("target/site/jacoco/jacoco.csv");
        
        if (!Files.exists(jacocoReport)) {
            System.err.println("JaCoCo report not found at: " + jacocoReport);
            return -1;
        }

        try (BufferedReader reader = Files.newBufferedReader(jacocoReport)) {
            String line;
            long totalInstructionMissed = 0;
            long totalInstructionCovered = 0;

            // Skip header line
            reader.readLine();

            // Read data lines - aggregate coverage
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("GROUP")) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        long instructionMissed = Long.parseLong(parts[4]);
                        long instructionCovered = Long.parseLong(parts[5]);
                        
                        totalInstructionMissed += instructionMissed;
                        totalInstructionCovered += instructionCovered;
                    } catch (NumberFormatException e) {
                        // Skip malformed lines
                    }
                }
            }

            // Calculate coverage percentage
            long totalInstructions = totalInstructionMissed + totalInstructionCovered;
            if (totalInstructions == 0) {
                return 0;
            }

            int coveragePercentage = (int) ((totalInstructionCovered * 100) / totalInstructions);
            return Math.max(0, Math.min(100, coveragePercentage));

        } catch (IOException e) {
            System.err.println("Failed to parse JaCoCo report: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Run Maven with JaCoCo to generate coverage report.
     * 
     * @param repoRoot Repository root directory
     * @return true if successful, false otherwise
     */
    public static boolean runJaCoCoAnalysis(Path repoRoot) {
        try {
            System.out.println("Running JaCoCo analysis with Maven...");
            System.out.println("This may take a minute...");

            // Determine the correct Maven command based on OS
            String[] command;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("win")) {
                command = new String[]{"mvn.cmd", "clean", "test", "jacoco:report"};
            } else {
                command = new String[]{"mvn", "clean", "test", "jacoco:report"};
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(repoRoot.toFile());
            pb.redirectErrorStream(true);
            
            // Inherit environment from current process
            pb.environment().putAll(System.getenv());

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Maven JaCoCo failed with exit code: " + exitCode);
                return false;
            }

            System.out.println("JaCoCo analysis complete.");
            return true;

        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to run JaCoCo: " + e.getMessage());
            return false;
        }
    }
}
