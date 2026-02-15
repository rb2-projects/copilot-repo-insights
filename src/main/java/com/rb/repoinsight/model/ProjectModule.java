package com.rb.repoinsight.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a module or package group within a project.
 */
public class ProjectModule {

    private String name;
    private String path;
    private String description; // Optional, from AI analysis
    private List<String> topLevelPackages = new ArrayList<>();

    public ProjectModule(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTopLevelPackages() {
        return topLevelPackages;
    }

    public void setTopLevelPackages(List<String> topLevelPackages) {
        this.topLevelPackages = topLevelPackages;
    }

    public void addTopLevelPackage(String pkg) {
        if (!topLevelPackages.contains(pkg)) {
            topLevelPackages.add(pkg);
        }
    }

    @Override
    public String toString() {
        return "ProjectModule{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", topLevelPackages=" + topLevelPackages.size() +
                '}';
    }
}
