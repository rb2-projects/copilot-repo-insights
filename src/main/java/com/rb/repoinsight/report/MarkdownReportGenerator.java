package com.rb.repoinsight.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.rb.repoinsight.model.RepoContext;

public class MarkdownReportGenerator {

    public void generate(RepoContext context, Path outputFile) {
        String report = buildReport(context);

        try {
            Files.writeString(outputFile, report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write report", e);
        }
    }

    private String buildReport(RepoContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append("# Repository Insight Report\n\n");

        writeProjectOverview(sb, context);
        writeDetectedInformation(sb, context);
        writeArchitectureOverview(sb, context);
        writeFindingsAndRecommendations(sb, context);
        writeLearnMoreSection(sb, context);
        writeExternalDependencies(sb, context);
        writeCapabilitiesAndHygiene(sb, context);
        writeCopilotInfo(sb);

        return sb.toString();
    }

    private void writeFindingsAndRecommendations(StringBuilder sb, RepoContext context) {
        sb.append(FindingsGenerator.generate(context));
    }

    private void writeProjectOverview(StringBuilder sb, RepoContext context) {
        sb.append("## Project Overview\n\n");

        if (context.isCopilotAvailable()) {
            sb.append(context.getCopilotOutput()).append("\n\n");
        } else {
            sb.append("> [!NOTE]\n");
            sb.append("> **AI-generated overview unavailable.**\n");
            sb.append("> Reason: ").append(context.getCopilotFailureReason()).append("\n\n");
            sb.append(DeterministicOverviewGenerator.generate(context)).append("\n\n");
        }

        sb.append("---\n\n");
        sb.append("### Enabling AI Insights\n\n");
        sb.append("- Install GitHub Copilot CLI\n");
        sb.append("- Authenticate via `copilot auth login`\n\n");
    }

    private void writeDetectedInformation(StringBuilder sb, RepoContext context) {
        sb.append("## Detected Information\n");
        sb.append("- Build tool: ").append(context.getBuildTool()).append("\n");
        sb.append("- Language: ").append(context.getLanguage()).append("\n");
        sb.append("- Tests present: ").append(context.hasTests()).append("\n");
        sb.append("- CI present: ").append(context.hasCi()).append("\n\n");
    }

    private void writeArchitectureOverview(StringBuilder sb, RepoContext context) {
        sb.append("## Architecture Overview\n\n");
        sb.append(MermaidGenerator.generate(context)).append("\n");
        sb.append("---\n\n");
    }

    private void writeLearnMoreSection(StringBuilder sb, RepoContext context) {
        sb.append("## Learn More with Copilot CLI\n\n");

        if (!context.hasCi()) {
            sb.append("### No CI/CD Pipeline Detected\n\n");
            sb.append("**Issue:** Continuous Integration helps catch bugs early and automate deployments.\n\n");
            sb.append("**Learn more with Copilot:**\n\n");
            sb.append("```bash\n");
            sb.append("copilot suggest --type chat \"How do I set up GitHub Actions CI for a Java/Maven project?\"\n");
            sb.append("```\n\n");
        }

        if (!context.hasTests()) {
            sb.append("### No Test Coverage Detected\n\n");
            sb.append("**Issue:** Tests ensure code reliability and enable confident refactoring.\n\n");
            sb.append("**Learn more with Copilot:**\n\n");
            sb.append("```bash\n");
            sb.append("copilot suggest --type chat \"What are best practices for Java unit testing? Show me a simple example.\"\n");
            sb.append("```\n\n");
        }

        sb.append("### Understanding the Architecture\n\n");
        sb.append("**Next steps:** Get detailed architectural guidance.\n\n");
        sb.append("**Ask Copilot:**\n\n");
        sb.append("```bash\n");
        sb.append("copilot suggest --type chat \"Explain the typical architecture for a repository analysis tool\"\n");
        sb.append("```\n\n");
    }

    private void writeExternalDependencies(StringBuilder sb, RepoContext context) {
        sb.append("## External Dependencies\n\n");

        if (context.getExternalDependencies().isEmpty()) {
            sb.append("No external systems or infrastructure dependencies detected.\n\n");
        } else {
            for (var dep : context.getExternalDependencies()) {
                sb.append("- **").append(dep.getName()).append("** (").append(dep.getCategory()).append(")\n");
                sb.append("  - Evidence: _").append(dep.getEvidence()).append("_\n");
            }
            sb.append("\n");
        }
    }

    private void writeCapabilitiesAndHygiene(StringBuilder sb, RepoContext context) {
        sb.append("## Capabilities & Hygiene\n\n");
        sb.append("| Area | Status | Notes |\n");
        sb.append("|------|--------|-------|\n");
        sb.append("| Build | ").append(context.getBuildTool() != null ? "✅" : "❌").append(" | ")
                .append(context.getBuildTool() != null ? context.getBuildTool() + " project detected" : "No build tool detected")
                .append(" |\n");
        sb.append("| Tests | ").append(context.hasTests() ? "✅" : "❌").append(" | ")
                .append(context.hasTests() ? "Test sources present" : "No tests detected")
                .append(" |\n");
        sb.append("| CI | ").append(context.hasCi() ? "✅" : "❌").append(" | ")
                .append(context.hasCi() ? "CI configuration found" : "No CI configuration found")
                .append(" |\n\n");
    }

    private void writeCopilotInfo(StringBuilder sb) {
        sb.append("## Copilot Integration (Optional)\n\n");
        sb.append("This tool can optionally use the GitHub Copilot CLI to generate a\n");
        sb.append("high-level, natural-language project overview.\n\n");
        sb.append("To enable this feature:\n\n");
        sb.append("1. Install GitHub Copilot CLI\n");
        sb.append("   https://github.com/features/copilot/download\n\n");
        sb.append("2. Authenticate with Copilot\n");
        sb.append("   ```bash\n");
        sb.append("   copilot auth login\n");
        sb.append("   ```\n\n");
        sb.append("3. Re-run the tool with: `--enable-ai`\n\n");
        sb.append("If Copilot is not available, the report is still generated using\n");
        sb.append("deterministic repository analysis.\n");
    }
}
