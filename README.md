# Copilot Repo Insight

**Turn repository analysis into actionable insights in seconds.**

A CLI tool that analyzes Java/Maven/Gradle projects and generates comprehensive insight reports. Works standalone with deterministic analysis, or supercharged with **GitHub Copilot CLI** for natural-language architectural summaries and AI-driven recommendations.

> Built for the [GitHub 2026 Copilot Challenge](https://dev.to/challenges/github-2026-01-21)

## Use Cases

- **New to a project?** Understand architecture, test coverage, and dependencies in minutes
- **Need to fix/enhance code?** Get full context before diving into unfamiliar code
- **Managing multiple repos?** Standardize insights and track quality across your organization
- **Tracking tech quality?** Assess architecture, test coverage, and tech debt across your portfolio

## Features

### Deterministic Analysis (Always Available)
- **Build & Language Detection** - Maven, Gradle, Java identification
- **Test Coverage Detection** - Accurate detection of test sources and test file count
- **CI/CD Detection** - GitHub Actions, GitLab CI, and other workflows
- **External Dependencies** - Parse and categorize external systems (databases, messaging, cloud services)
- **Architecture Analysis** - Identify architectural patterns and risks
- **Quality Metrics** - Lines of code, package structure, code distribution
- **Findings & Recommendations** - Automated suggestions for improvements

### AI-Enhanced Features (With GitHub Copilot CLI)
- **Natural Language Summary** - GitHub Copilot CLI-generated architectural overview in plain English
- **AI-Driven Risk Assessment** - Intelligent analysis of architectural risks and patterns
- **Smart Recommendations** - Context-aware improvement suggestions powered by Copilot
- **Technology Stack Interpretation** - What the tech stack means and how it fits together

### Report Formats
- **Markdown Reports** - Git-friendly, readable format
- **Mermaid Diagrams** - Visual representations of project hygiene and architecture

## Quick Start

### Installation

```bash
git clone https://github.com/yourusername/copilot-repo-insight.git
cd copilot-repo-insight
mvn clean package
```

### Basic Usage (Deterministic Only)

```bash
java -jar target/copilot-repo-insight-0.1.0.jar
```

This generates:
- `repo-insight.md` - Markdown report with deterministic analysis

### Enhanced Usage (With GitHub Copilot CLI)

```bash
# First, ensure GitHub Copilot CLI is installed and authenticated
curl https://raw.githubusercontent.com/github-copilot/cli/main/install.sh | sh
copilot auth login

# Then run with AI enhancement
java -jar target/copilot-repo-insight-0.1.0.jar --enable-ai
```

This adds:
- AI-generated project overview powered by GitHub Copilot
- Intelligent architectural analysis
- Context-aware recommendations

## Report Contents

Generated reports include:

1. **Project Overview** - Build tool, language, and basic metadata
2. **Architecture Assessment** - Patterns identified, confidence levels, and architectural style
3. **Risk Analysis** - Top architectural risks with impact assessment
4. **Findings & Recommendations** - Specific findings and prioritized improvement suggestions
5. **Architecture Diagrams** - Mermaid visualizations of project hygiene and patterns
6. **Capabilities Matrix** - Quick overview of detected capabilities (tests, CI, frameworks, etc.)
7. **External Dependencies** - All detected external systems the project depends on
8. **Maintainability Rating** - Overall assessment of code maintainability

## Quality Assurance

This project practices what it preaches:
- **50+ Unit Tests** covering core functionality
- **Test Coverage** for repository scanning, report generation, and data models
- **CI-Ready** - Maven build with test execution
- **Clean Code** - Organized package structure with clear separation of concerns

Run tests locally:
```bash
mvn clean test
```

## How It Works

1. **Scan** - Analyzes repository structure, build files, and source code
2. **Analyze** - Deterministically detects capabilities, patterns, and risks
3. **Enhance** (Optional) - Uses Copilot CLI to generate natural-language insights
4. **Report** - Generates beautiful, actionable reports in multiple formats

## Supported Project Types

- **Maven Projects** (pom.xml)
- **Gradle Projects** (build.gradle, build.gradle.kts)
- **Java Source Code** (automatic detection)

## What Gets Detected

### Build & Tooling
- Build system (Maven, Gradle)
- Programming language
- Test framework presence
- CI/CD pipeline configuration

### Architecture
- External dependencies and systems
- Technology stack (Spring, databases, messaging, cloud services)
- Package structure
- Code distribution (main vs. test)

### Quality
- Test coverage indicators
- Architecture patterns
- Potential architectural risks
- Code organization metrics

## Example Output

### Without Copilot:
```
Detected:
- Build tool: Maven
- Language: Java
- Tests present: true
- CI present: false
```

### With Copilot (--enable-ai):
```
Detected:
- Build tool: Maven
- Language: Java
- Tests present: true
- CI present: false

Project Overview:
[AI-generated natural language summary of architecture]

Top Architectural Risks:
[AI-identified patterns and concerns]
```

## Contributing

Contributions welcome! Areas for enhancement:
- Additional language support
- More sophisticated architecture pattern detection
- Enhanced AI prompt templates
- Better visualization options

## License

[Include your license here]

## Learning Resources

- [GitHub Copilot CLI Official Repository](https://github.com/github-copilot/cli)
- [GitHub Copilot CLI Getting Started](https://docs.github.com/en/copilot/github-copilot-in-the-cli/using-github-copilot-in-the-cli)
- [Maven Documentation](https://maven.apache.org/)
- [Gradle Documentation](https://gradle.org/)
