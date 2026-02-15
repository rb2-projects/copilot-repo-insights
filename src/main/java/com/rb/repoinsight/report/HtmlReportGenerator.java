package com.rb.repoinsight.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.rb.repoinsight.model.RepoContext;

public class HtmlReportGenerator {

    public void generate(RepoContext context, Path outputFile) {
        String report = buildReport(context);
        try {
            Files.writeString(outputFile, report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML report", e);
        }
    }

    private String buildReport(RepoContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"><title>Repository Insight</title></head><body>");
        sb.append("<h1>Repository Insight Report</h1>");
        sb.append("<p>Build Tool: ").append(escapeHtml(context.getBuildTool())).append("</p>");
        sb.append("<p>Language: ").append(escapeHtml(context.getLanguage())).append("</p>");
        sb.append("<p>Test Coverage: ").append(context.getTestCoveragePercentage()).append("%</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
