package com.rb.repoinsight.ai;

import java.io.IOException;

import com.rb.repoinsight.util.SystemCommandExecutor;
import com.rb.repoinsight.util.SystemCommandExecutor.CommandResult;

/**
 * GitHub Copilot CLI implementation of AiClient.
 * Uses the `copilot` binary in programmatic mode.
 */
public class CopilotClient implements AiClient {

    private final SystemCommandExecutor executor;
    private String unavailabilityReason;

    public CopilotClient(SystemCommandExecutor executor) {
        this.executor = executor;
        checkAvailability();
    }

    private void checkAvailability() {
        try {
            // Check if Copilot CLI is installed
            CommandResult check = executor.execute("copilot", "--version");

            if (!check.isSuccess()) {
                unavailabilityReason = "GitHub Copilot CLI check failed: " + check.getStderr();
                return;
            }

            unavailabilityReason = null;

        } catch (IOException e) {
            unavailabilityReason = "Failed to check Copilot CLI availability: " + e.getMessage();
        }
    }

    @Override
    public boolean isAvailable() {
        return unavailabilityReason == null;
    }

    @Override
    public String getUnavailabilityReason() {
        return unavailabilityReason;
    }

    @Override
    public String analyze(String prompt) {
        if (!isAvailable()) {
            return "AI Analysis Unavailable: " + unavailabilityReason;
        }

        try {
            // Use -p flag for prompt and -s flag to suppress stats
            // Add flags to disable agentic mode that causes tool exploration
            CommandResult result = executor.execute(
                    "copilot",
                    "-p",
                    prompt,
                    "-s",
                    "--disable-builtin-mcps",
                    "--no-ask-user");

            if (result.isSuccess()) {
                String output = result.getStdout().trim();

                if (output.isEmpty()) {
                    return "AI Analysis Failed: Empty response from Copilot.";
                }

                return output;
            } else {
                return "AI Analysis Failed: " + result.getStderr();
            }

        } catch (IOException e) {
            return "AI Analysis Failed: " + e.getMessage();
        }
    }
}
