package com.rb.repoinsight.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rb.repoinsight.model.RepoContext;

/**
 * Unit tests for RepoScanner.
 */
class RepoScannerTest {

    private RepoScanner scanner;

    @BeforeEach
    void setUp() {
        scanner = new RepoScanner();
    }

    @Test
    void testScanDetectsMavenProject(@TempDir Path tempDir) throws IOException {
        // Create a minimal Maven project structure
        Files.createFile(tempDir.resolve("pom.xml"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertNotNull(context);
        assertEquals("Maven", context.getBuildTool());
    }

    @Test
    void testScanDetectsJavaLanguage(@TempDir Path tempDir) throws IOException {
        // Create Java source file
        Path srcDir = tempDir.resolve("src/main/java");
        Files.createDirectories(srcDir);
        Files.createFile(srcDir.resolve("Main.java"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertEquals("Java", context.getLanguage());
    }

    @Test
    void testScanDetectsTestsWhenJavaTestFilesExist(@TempDir Path tempDir) throws IOException {
        // Create test directory with actual test files
        Path testDir = tempDir.resolve("src/test/java");
        Files.createDirectories(testDir);
        Files.createFile(testDir.resolve("SampleTest.java"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertTrue(context.hasTests());
    }

    @Test
    void testScanReportsNoTestsWhenTestDirIsEmpty(@TempDir Path tempDir) throws IOException {
        // Create empty test directory
        Path testDir = tempDir.resolve("src/test");
        Files.createDirectories(testDir);
        
        RepoContext context = scanner.scan(tempDir);
        
        assertFalse(context.hasTests());
    }

    @Test
    void testScanReportsNoTestsWhenTestDirDoesNotExist(@TempDir Path tempDir) {
        // Don't create test directory at all
        RepoContext context = scanner.scan(tempDir);
        
        assertFalse(context.hasTests());
    }

    @Test
    void testScanDetectsCiWithGithubWorkflows(@TempDir Path tempDir) throws IOException {
        // Create GitHub workflows directory
        Path workflowDir = tempDir.resolve(".github/workflows");
        Files.createDirectories(workflowDir);
        Files.createFile(workflowDir.resolve("build.yml"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertTrue(context.hasCi());
    }

    @Test
    void testScanDetectsCiWithGitlabCI(@TempDir Path tempDir) throws IOException {
        // Create GitLab CI file
        Files.createFile(tempDir.resolve(".gitlab-ci.yml"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertTrue(context.hasCi());
    }

    @Test
    void testScanReportsNoCiWhenNoConfigFound(@TempDir Path tempDir) {
        // Don't create any CI configuration
        RepoContext context = scanner.scan(tempDir);
        
        assertFalse(context.hasCi());
    }

    @Test
    void testScanSetsRepoPath(@TempDir Path tempDir) {
        RepoContext context = scanner.scan(tempDir);
        
        assertNotNull(context.getRepoPath());
        assertTrue(context.getRepoPath().contains(tempDir.getFileName().toString()));
    }

    @Test
    void testScanInitializesExternalDependencies(@TempDir Path tempDir) {
        RepoContext context = scanner.scan(tempDir);
        
        assertNotNull(context.getExternalDependencies());
    }

    @Test
    void testScanDetectsGradleBuild(@TempDir Path tempDir) throws IOException {
        // Create Gradle build file
        Files.createFile(tempDir.resolve("build.gradle"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertEquals("Gradle", context.getBuildTool());
    }

    @Test
    void testScanDetectsKotlinGradleBuild(@TempDir Path tempDir) throws IOException {
        // Create Kotlin Gradle build file
        Files.createFile(tempDir.resolve("build.gradle.kts"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertEquals("Gradle", context.getBuildTool());
    }

    @Test
    void testScanHandlesMultipleJavaTestFiles(@TempDir Path tempDir) throws IOException {
        // Create test directory with multiple test files
        Path testDir = tempDir.resolve("src/test/java/com/example");
        Files.createDirectories(testDir);
        Files.createFile(testDir.resolve("Test1.java"));
        Files.createFile(testDir.resolve("Test2.java"));
        Files.createFile(testDir.resolve("Test3.java"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertTrue(context.hasTests());
    }

    @Test
    void testScanIgnoresNonJavaFilesInTestDir(@TempDir Path tempDir) throws IOException {
        // Create test directory with only non-Java files
        Path testDir = tempDir.resolve("src/test/resources");
        Files.createDirectories(testDir);
        Files.createFile(testDir.resolve("config.xml"));
        
        RepoContext context = scanner.scan(tempDir);
        
        assertFalse(context.hasTests());
    }
}
