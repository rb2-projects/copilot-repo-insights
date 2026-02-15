package com.rb.repoinsight.util;

import java.io.IOException;

/**
 * Interface for executing system commands.
 * Abstracts ProcessBuilder to enable unit testing without actual command
 * execution.
 */
public interface SystemCommandExecutor {

    /**
     * Result of a command execution.
     */
    class CommandResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public CommandResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public int getExitCode() {
            return exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    /**
     * Executes a system command.
     *
     * @param command The command and its arguments
     * @return The command result
     * @throws IOException if command execution fails
     */
    CommandResult execute(String... command) throws IOException;
}
