package com.rb.repoinsight.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConfig {
    private String name;
    private DependencyCategory category;
    private List<String> mavenArtifacts = new ArrayList<>();
    private List<String> heuristics = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DependencyCategory getCategory() {
        return category;
    }

    public void setCategory(DependencyCategory category) {
        this.category = category;
    }

    public List<String> getMavenArtifacts() {
        return mavenArtifacts;
    }

    public void setMavenArtifacts(List<String> mavenArtifacts) {
        this.mavenArtifacts = mavenArtifacts;
    }

    public List<String> getHeuristics() {
        return heuristics;
    }

    public void setHeuristics(List<String> heuristics) {
        this.heuristics = heuristics;
    }
}
