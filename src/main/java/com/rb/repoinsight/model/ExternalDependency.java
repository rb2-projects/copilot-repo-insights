package com.rb.repoinsight.model;

public class ExternalDependency {

    private final String name;
    private final DependencyCategory category;
    private final String evidence;

    public ExternalDependency(String name, String category, String evidence) {
        this(name, DependencyCategory.fromString(category), evidence);
    }

    public ExternalDependency(String name, DependencyCategory category, String evidence) {
        this.name = name;
        this.category = category;
        this.evidence = evidence;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category.getDisplayName();
    }

    public DependencyCategory getCategoryEnum() {
        return category;
    }

    public String getEvidence() {
        return evidence;
    }
}
