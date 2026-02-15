package com.rb.repoinsight.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RepoContext model class.
 */
class RepoContextTest {

    private RepoContext context;

    @BeforeEach
    void setUp() {
        context = new RepoContext();
    }

    @Test
    void testDefaultValuesAreInitialized() {
        assertNull(context.getBuildTool());
        assertNull(context.getLanguage());
        assertFalse(context.hasTests());
        assertFalse(context.hasCi());
        assertNotNull(context.getExternalDependencies());
        assertTrue(context.getExternalDependencies().isEmpty());
    }

    @Test
    void testSetAndGetBuildTool() {
        context.setBuildTool("Maven");
        assertEquals("Maven", context.getBuildTool());
    }

    @Test
    void testSetAndGetLanguage() {
        context.setLanguage("Java");
        assertEquals("Java", context.getLanguage());
    }

    @Test
    void testSetAndGetTestsFlag() {
        assertFalse(context.hasTests());
        context.setHasTests(true);
        assertTrue(context.hasTests());
    }

    @Test
    void testSetAndGetCiFlag() {
        assertFalse(context.hasCi());
        context.setHasCi(true);
        assertTrue(context.hasCi());
    }

    @Test
    void testSetAndGetRepoPath() {
        String path = "/path/to/repo";
        context.setRepoPath(path);
        assertEquals(path, context.getRepoPath());
    }

    @Test
    void testExternalDependenciesCanBeAdded() {
        ExternalDependency dep = new ExternalDependency("Spring", "Framework", "pom.xml");
        context.getExternalDependencies().add(dep);
        assertEquals(1, context.getExternalDependencies().size());
        assertEquals("Spring", context.getExternalDependencies().get(0).getName());
    }

    @Test
    void testCopilotAvailability() {
        assertFalse(context.isCopilotAvailable());
        context.setCopilotAvailable(true);
        assertTrue(context.isCopilotAvailable());
    }

    @Test
    void testCopilotOutput() {
        String output = "Test output";
        context.setCopilotOutput(output);
        assertEquals(output, context.getCopilotOutput());
    }

    @Test
    void testPackagingType() {
        context.setPackagingType("jar");
        assertEquals("jar", context.getPackagingType());
    }
}
