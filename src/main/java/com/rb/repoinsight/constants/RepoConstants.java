package com.rb.repoinsight.constants;

import java.util.List;

public final class RepoConstants {

        private RepoConstants() {
        }

        // --------------------------------------------------
        // Languages
        // --------------------------------------------------

        public static final String LANGUAGE_JAVA = "Java";
        public static final String LANGUAGE_KOTLIN = "Kotlin";
        public static final String LANGUAGE_UNKNOWN = "UNKNOWN";

        // --------------------------------------------------
        // Build tools
        // --------------------------------------------------

        public static final String BUILD_TOOL_MAVEN = "Maven";
        public static final String BUILD_TOOL_GRADLE = "Gradle";

        // --------------------------------------------------
        // Packaging types
        // --------------------------------------------------

        public static final String PACKAGING_JAR = "JAR";
        public static final String PACKAGING_WAR = "WAR";
        public static final String PACKAGING_UNKNOWN = "UNKNOWN";

        // --------------------------------------------------
        // Framework identifiers
        // --------------------------------------------------

        public static final List<String> SPRING_IDENTIFIERS = List.of(
                        "spring-boot",
                        "spring-context",
                        "org.springframework");

        // --------------------------------------------------
        // External system heuristics
        // --------------------------------------------------

        public static final List<String> DATABASE_IDENTIFIERS = List.of(
                        "jdbc",
                        "hibernate",
                        "jpa",
                        "spring-data");
}
