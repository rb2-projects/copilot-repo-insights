package com.rb.repoinsight.model;

/**
 * Enumeration of external dependency categories for consistent classification.
 */
public enum DependencyCategory {
    PERSISTENCE("Persistence"),
    MESSAGING("Messaging"),
    CLOUD_SERVICES("Cloud Services"),
    WEB("Web"),
    UNKNOWN("Unknown");

    private final String displayName;

    DependencyCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Finds a category by its display name (case-insensitive).
     */
    public static DependencyCategory fromString(String category) {
        for (DependencyCategory c : values()) {
            if (c.displayName.equalsIgnoreCase(category)) {
                return c;
            }
        }
        return UNKNOWN;
    }
}
