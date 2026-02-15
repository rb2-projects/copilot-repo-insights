package com.rb.repoinsight.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.rb.repoinsight.model.ExternalDependency;
import com.rb.repoinsight.model.RepoContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MarkdownReportGenerator.
 */
class MarkdownReportGeneratorTest {

    private MarkdownReportGenerator generator;
    private RepoContext context;

    @BeforeEach
    void setUp() {
        generator = new MarkdownReportGenerator();
        context = new RepoContext();
        context.setBuildTool("Maven");
        context.setLanguage("Java");
        context.setHasTests(true);
        context.setHasCi(false);
    }

    @Test
    void testGenerateCreatesMarkdownFile(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        assertTrue(Files.exists(outputFile));
        String content = Files.readString(outputFile);
        assertNotNull(content);
        assertFalse(content.isEmpty());
    }

    @Test
    void testGenerateIncludesProjectTitle(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Repository Insight Report") || content.contains("Project Overview"));
    }

    @Test
    void testGenerateIncludesDetectedInformation(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Maven"));
        assertTrue(content.contains("Java"));
    }

    @Test
    void testGenerateWithExternalDependencies(@TempDir Path tempDir) throws IOException {
        ExternalDependency dep = new ExternalDependency("Spring", "Framework", "pom.xml");
        context.getExternalDependencies().add(dep);
        
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("Spring") || content.contains("Dependency"));
    }

    @Test
    void testGenerateWithTestsAbsent(@TempDir Path tempDir) throws IOException {
        context.setHasTests(false);
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("false") || content.contains("❌"));
    }

    @Test
    void testGenerateWithCiPresent(@TempDir Path tempDir) throws IOException {
        context.setHasCi(true);
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        assertTrue(content.contains("true") || content.contains("✅"));
    }

    @Test
    void testGenerateHandlesNullBuildTool(@TempDir Path tempDir) throws IOException {
        context.setBuildTool(null);
        Path outputFile = tempDir.resolve("test-report.md");
        
        // Should not throw exception
        assertDoesNotThrow(() -> generator.generate(context, outputFile));
        assertTrue(Files.exists(outputFile));
    }

    @Test
    void testGenerateProducesValidMarkdown(@TempDir Path tempDir) throws IOException {
        Path outputFile = tempDir.resolve("test-report.md");
        generator.generate(context, outputFile);
        
        String content = Files.readString(outputFile);
        // Check for basic markdown structure
        assertTrue(content.contains("#")); // Headers
    }
}
