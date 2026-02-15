package com.rb.repoinsight.ai;

/**
 * Interface for AI-based repository analysis.
 * Abstracts the AI provider to allow future implementations beyond GitHub
 * Copilot.
 */
public interface AiClient {

    /**
     * Analyzes the repository based on the provided prompt.
     *
     * @param prompt The analysis prompt containing repository metadata
     * @return The AI-generated analysis, or an error message if analysis fails
     */
    String analyze(String prompt);

    /**
     * Checks if the AI client is available and properly configured.
     *
     * @return true if the client can perform analysis, false otherwise
     */
    boolean isAvailable();

    /**
     * Returns a human-readable reason for unavailability.
     *
     * @return Error message if unavailable, null if available
     */
    String getUnavailabilityReason();
}
