package com.rb.repoinsight.constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.rb.repoinsight.model.RepoContext;

/**
 * Centralized configuration loader for all AI prompts used in Repo Insight.
 * Prompts are loaded from prompts.properties file, allowing customization without recompilation.
 * This approach maintains version control history, enables easy auditing, and supports prompt tuning.
 */
public class PromptsConfig {

    private static final Logger LOGGER = Logger.getLogger(PromptsConfig.class.getName());
    private static final Properties properties = loadProperties();

    /**
     * Prompt for generating a short architectural overview via copilot CLI.
     * Used when scanning the repository structure.
     */
    public static final String ARCHITECTURAL_OVERVIEW_PROMPT = getProperty("prompt.architectural.overview");

    /**
     * Comprehensive analysis prompt template for repository assessment.
     * Used to generate detailed architectural analysis with risk assessment.
     */
    public static final String COMPREHENSIVE_ANALYSIS_TEMPLATE = getProperty("prompt.comprehensive.analysis");

    /**
     * Load properties from prompts.properties file.
     * Falls back to empty properties if file not found.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream is = PromptsConfig.class.getResourceAsStream("/prompts.properties")) {
            if (is != null) {
                props.load(is);
                LOGGER.info("Prompts loaded from prompts.properties");
            } else {
                LOGGER.warning("prompts.properties not found in resources");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load prompts.properties: {0}", e.getMessage());
        }
        return props;
    }

    /**
     * Retrieve a prompt from properties with fallback to default.
     */
    private static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            LOGGER.log(Level.WARNING, "Prompt key not found: {0}", key);
            return "";
        }
        // Handle escaped newlines in properties file
        return value.replace("\\n", "\n");
    }

    /**
     * Prompt for generating a concise project overview based on detected facts.
     * Variables: buildTool, language, hasTests, hasCi, usesSpring, hasDatabaseIntegration
     */
    public static String buildProjectOverviewPrompt(RepoContext context) {
        String template = getProperty("prompt.project.overview");
        return template.formatted(
                context.getBuildTool(),
                context.getLanguage(),
                context.hasTests(),
                context.hasCi(),
                context.isUsesSpring(),
                context.isHasDatabaseIntegration());
    }

}
