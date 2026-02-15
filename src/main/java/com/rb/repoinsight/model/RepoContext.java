package com.rb.repoinsight.model;

import java.util.ArrayList;
import java.util.List;

public class RepoContext {

    private String repoPath;
    private String buildTool;
    private String language;
    private boolean hasTests;
    private boolean hasCi;
    private String packagingType;
    private boolean usesSpring;
    private boolean hasDatabaseIntegration;
    private List<ExternalDependency> externalDependencies = new ArrayList<>();

    public List<ExternalDependency> getExternalDependencies() {
        return externalDependencies;
    }

    public void setExternalDependencies(List<ExternalDependency> externalDependencies) {
        this.externalDependencies = externalDependencies;
    }

    public boolean isCopilotAvailable() {
        return copilotAvailable;
    }

    public void setCopilotAvailable(boolean copilotAvailable) {
        this.copilotAvailable = copilotAvailable;
    }

    public String getCopilotFailureReason() {
        return copilotFailureReason;
    }

    public void setCopilotFailureReason(String copilotFailureReason) {
        this.copilotFailureReason = copilotFailureReason;
    }

    public String getCopilotOutput() {
        return copilotOutput;
    }

    public void setCopilotOutput(String copilotOutput) {
        this.copilotOutput = copilotOutput;
    }

    private boolean copilotAvailable;
    private String copilotFailureReason;
    private String copilotOutput;
    private int testCoveragePercentage;
    private boolean accurateCoverageAvailable;
    private List<String> complexitySignals = new ArrayList<>();
    private List<String> maintainabilityConcerns = new ArrayList<>();

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }

    public boolean isUsesSpring() {
        return usesSpring;
    }

    public void setUsesSpring(boolean usesSpring) {
        this.usesSpring = usesSpring;
    }

    public boolean isHasDatabaseIntegration() {
        return hasDatabaseIntegration;
    }

    public void setHasDatabaseIntegration(boolean hasDatabaseIntegration) {
        this.hasDatabaseIntegration = hasDatabaseIntegration;
    }

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

    public boolean hasTests() {
        return hasTests;
    }

    public void setHasTests(boolean hasTests) {
        this.hasTests = hasTests;
    }

    public boolean hasCi() {
        return hasCi;
    }

    public void setHasCi(boolean hasCi) {
        this.hasCi = hasCi;
    }

    public int getTestCoveragePercentage() {
        return testCoveragePercentage;
    }

    public void setTestCoveragePercentage(int testCoveragePercentage) {
        this.testCoveragePercentage = testCoveragePercentage;
    }

    public boolean isAccurateCoverageAvailable() {
        return accurateCoverageAvailable;
    }

    public void setAccurateCoverageAvailable(boolean accurateCoverageAvailable) {
        this.accurateCoverageAvailable = accurateCoverageAvailable;
    }

    public List<String> getComplexitySignals() {
        return complexitySignals;
    }

    public void setComplexitySignals(List<String> complexitySignals) {
        this.complexitySignals = complexitySignals;
    }

    public List<String> getMaintainabilityConcerns() {
        return maintainabilityConcerns;
    }

    public void setMaintainabilityConcerns(List<String> maintainabilityConcerns) {
        this.maintainabilityConcerns = maintainabilityConcerns;
    }

    private long generationTime = 0;
    private List<ProjectModule> projectModules = new ArrayList<>();

    public long getGenerationTime() {
        return generationTime;
    }

    public void setGenerationTime(long generationTime) {
        this.generationTime = generationTime;
    }

    public List<ProjectModule> getProjectModules() {
        return projectModules;
    }

    public void setProjectModules(List<ProjectModule> projectModules) {
        this.projectModules = projectModules;
    }
}