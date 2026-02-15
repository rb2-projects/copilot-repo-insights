package com.rb.repoinsight.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Production implementation of SystemCommandExecutor using ProcessBuilder.
 */
public class ProcessBuilderCommandExecutor implements SystemCommandExecutor {

    @Override
    public CommandResult execute(String... command) throws IOException {
        // On Windows, wrap command with cmd /c for better shell handling
        String[] resolvedCommand = command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            if (command.length > 0 && command[0].equals("copilot")) {
                // Special handling for copilot - use cmd.exe to avoid argument parsing issues
                String[] newCommand = new String[command.length + 2];
                newCommand[0] = "cmd";
                newCommand[1] = "/c";
                System.arraycopy(command, 0, newCommand, 2, command.length);
                resolvedCommand = newCommand;
            } else if (command.length > 0) {
                // Try to resolve executable for other commands
                String executable = command[0];
                String resolvedPath = findExecutableOnWindows(executable);
                if (resolvedPath != null) {
                    resolvedCommand = command.clone();
                    resolvedCommand[0] = resolvedPath;
                }
            }
        }

        ProcessBuilder pb = new ProcessBuilder(resolvedCommand);
        pb.redirectErrorStream(false);

        Process process = pb.start();

        String stdout;
        String stderr;

        try (BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            stdout = stdoutReader.lines().collect(Collectors.joining("\n"));
            stderr = stderrReader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command execution interrupted", e);
        }

        return new CommandResult(exitCode, stdout, stderr);
    }

    private String findExecutableOnWindows(String executable) {
        // Don't try to resolve if it's already a full path
        if (executable.contains("\\") || executable.contains("/")) {
            return null;
        }

        // Add .exe if not present
        String exeName = executable.endsWith(".exe") ? executable : executable + ".exe";

        // Check common Windows locations for user-installed binaries
        String userProfile = System.getenv("USERPROFILE");
        if (userProfile != null) {
            // Check WinGet packages location
            java.nio.file.Path wingetPath = java.nio.file.Paths.get(
                    userProfile,
                    "AppData", "Local", "Microsoft", "WinGet", "Packages");

            if (java.nio.file.Files.exists(wingetPath)) {
                try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(wingetPath, 2)) {
                    java.util.Optional<java.nio.file.Path> found = paths
                            .filter(p -> p.getFileName().toString().equalsIgnoreCase(exeName))
                            .findFirst();
                    if (found.isPresent()) {
                        return found.get().toString();
                    }
                } catch (IOException e) {
                    // Ignore and continue
                }
            }

            // Check AppData\Local\Programs
            java.nio.file.Path localPrograms = java.nio.file.Paths.get(
                    userProfile, "AppData", "Local", "Programs", exeName);
            if (java.nio.file.Files.exists(localPrograms)) {
                return localPrograms.toString();
            }
        }

        return null;
    }
}
