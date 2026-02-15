# Configuration Guide for Complexity Analysis

## Configurable Thresholds

The repository analysis tool includes configurable thresholds for detecting complexity signals and maintainability concerns. Adjust these values in `ComplexityThresholds.java` to customize the analysis for your codebase.

### Default Thresholds

```java
// File size threshold
LARGE_FILE_LINES = 300          // Files > 300 LOC flagged as large

// Method/class size threshold  
LONG_METHOD_LINES = 40          // Methods/classes > 40 LOC flagged as long

// Test coverage thresholds
TEST_COVERAGE_GREEN = 80        // 80%+ = Green (optimal)
TEST_COVERAGE_YELLOW_MIN = 50   // 50-80% = Yellow (warning)
                                // <50% = Red (critical)
```

### Adjusting Thresholds

Edit `src/main/java/com/rb/repoinsight/constants/ComplexityThresholds.java`:

```java
public class ComplexityThresholds {
    // Increase for stricter analysis (flags more files)
    public static final int LARGE_FILE_LINES = 400;  // Changed from 300
    
    // Increase for stricter method analysis
    public static final int LONG_METHOD_LINES = 50;  // Changed from 40
    
    // Adjust coverage thresholds to your team's standards
    public static final int TEST_COVERAGE_GREEN = 75;      // Changed from 80
    public static final int TEST_COVERAGE_YELLOW_MIN = 40; // Changed from 50
}
```

Then rebuild: `mvn clean package -DskipTests`

## Test Coverage Calculation

The tool uses a **heuristic approach** to calculate approximate test coverage:

1. **Automatic (always runs)**: Smart class matching between source and test code
   - Labeled "approximate" in reports
   - Based on test class count vs source class count ratio
   - Useful for quick project assessment

2. **Accurate (optional)**: JaCoCo integration for precise coverage
   - Use flag: `--coverage-accurate` or `--enable-coverage` (implementation pending)
   - Provides "accurate" label in reports
   - Requires project to support JaCoCo

## Complexity Analysis Details

### Signals Detected
- **Large files**: Source files exceeding `LARGE_FILE_LINES`
- **Long methods**: Methods exceeding `LONG_METHOD_LINES` 
- **Untested large files**: Large files in projects with low test coverage
- **Low/no test coverage**: When `<50%` or no tests present

### Concerns Scored
- **Critical**: No test suite present
- **Warning**: Low coverage (below optimal thresholds)
- **Info**: Large codebase considerations

## Report Output

### Markdown Report
- **Summary Grid**: Test coverage shown as percentage with color coding
- **Project Metrics Section**: 
  - Test coverage with accuracy indicator
  - Complexity signals list
  - Tiered maintainability concerns
- **Architecture**: Shows Mermaid diagrams for data flow and module architecture
- **Learn More with Copilot CLI section**: Actionable prompts for users

## Industry Standards Reference

The default thresholds are based on industry best practices:

| Metric | Industry Standard | Tool Default |
|--------|------------------|--------------|
| Test Coverage - Green | >80% | >80% |
| Test Coverage - Warning | 50-80% | 50-80% |
| Test Coverage - Critical | <50% | <50% |
| Large File | 300-500 LOC | 300 LOC |
| Long Method | 20-50 LOC | 40 LOC |

## Running Analysis

```bash
# Deterministic analysis (always works)
java -jar copilot-repo-insight-0.1.0.jar

# With AI-enhanced analysis (optional)
java -jar copilot-repo-insight-0.1.0.jar --enable-ai

# With accurate test coverage (future)
java -jar copilot-repo-insight-0.1.0.jar --coverage-accurate
```

## Customization Examples

### Conservative Analysis (fewer flags)
```java
LARGE_FILE_LINES = 500;          // Only flag very large files
LONG_METHOD_LINES = 100;         // Only flag very long methods
TEST_COVERAGE_GREEN = 60;        // Lower threshold for green
```

### Strict Analysis (more flags)
```java
LARGE_FILE_LINES = 200;          // Flag even moderately large files
LONG_METHOD_LINES = 25;          // Flag moderately long methods
TEST_COVERAGE_GREEN = 90;        // Higher threshold for green
```

### Custom for Legacy Codebases
```java
LARGE_FILE_LINES = 1000;         // Legacy code is typically larger
LONG_METHOD_LINES = 200;         // Long methods are common
TEST_COVERAGE_GREEN = 40;        // Starting point for coverage
TEST_COVERAGE_YELLOW_MIN = 20;   // Accept lower baseline
```
