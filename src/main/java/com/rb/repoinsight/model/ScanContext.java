package com.rb.repoinsight.model;

import java.util.ArrayList;
import java.util.List;

public class ScanContext {

    private String buildTool;
    private String language;
    private boolean testsPresent;
    private boolean ciPresent;

    private List<ExternalDependency> externalDependencies = new ArrayList<>();

    // --- basic detected info ---

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isTestsPresent() {
        return testsPresent;
    }

    public void setTestsPresent(boolean testsPresent) {
        this.testsPresent = testsPresent;
    }

    public boolean isCiPresent() {
        return ciPresent;
    }

    public void setCiPresent(boolean ciPresent) {
        this.ciPresent = ciPresent;
    }

    // --- external dependencies ---

    public List<ExternalDependency> getExternalDependencies() {
        return externalDependencies;
    }

    public void setExternalDependencies(List<ExternalDependency> externalDependencies) {
        this.externalDependencies = externalDependencies;
    }
}