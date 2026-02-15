package com.rb.repoinsight.copilot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class CopilotClient {

    private CopilotClient() {
    }

    public static String generateOverview(String prompt) {
        try {
            Process process = new ProcessBuilder(
                    "copilot",
                    "-p",
                    prompt,
                    "--format",
                    "text")
                    .redirectErrorStream(true)
                    .start();

            StringBuilder output = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0 || output.toString().trim().isEmpty()) {
                return FALLBACK_TEXT;
            }

            return output.toString().trim();

        } catch (IOException | InterruptedException e) {
            return FALLBACK_TEXT;
        }
    }

    private static final String FALLBACK_TEXT = """
            ℹ️ AI-generated project overview unavailable.

            This feature requires:
            - GitHub CLI
            - GitHub Copilot CLI extension
            - An authenticated GitHub session

            The remainder of this report is generated deterministically.
            """.trim();
}
