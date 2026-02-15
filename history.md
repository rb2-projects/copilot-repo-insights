# Copilot Repo Insight

## Overview
Copilot Repo Insight is a **static repository analysis tool** aimed at producing a **human-readable architecture and production-readiness report** for a codebase, without requiring the service to be built or run.

The tool is intentionally:
- **Deterministic** (no LLM required for core results)
- **Fast** (simple filesystem + text parsing)
- **Static** (no runtime introspection)
- **Architecture/SRE-focused**, not just dependency listing

Optional AI integration (via GitHub Copilot CLI) is supported for **natural-language project summaries**, but the core value comes from deterministic analysis.

This README is written as a **handover document to another AI (Gemini 3 / Antigravity)** and is intentionally explicit about intent, design decisions, limitations, and next steps.

---

## Current State (What Exists Today)

### Inputs
- A local source repository (currently Java/Maven-focused)
- No requirement for Maven execution or dependency resolution
- Optional GitHub CLI + Copilot CLI for AI-generated overview

### Outputs
A markdown report (`Repository Insight Report`) containing:
- Project metadata (language, build tool)
- Presence of tests
- Presence of CI configuration
- (Attempted) detection of external dependencies

### Deterministic Detection Implemented
- Build tool detection (Maven)
- Language detection (Java)
- Test source presence
- CI presence (basic heuristics)

### Optional AI Feature
- If GitHub Copilot CLI is authenticated, an LLM-generated project overview is included
- If not, the tool degrades gracefully

---

## Known Limitation (Critical)

### External Dependency Detection Is Naive

The current implementation **only checks for a small, hard-coded list of strings** inside `pom.xml` / `build.gradle`.

Example (simplified):
- If `spring-kafka` → Kafka detected
- If `spring-boot-starter-data-jpa` → Database detected

This causes **false negatives** when:
- The project uses other Spring starters
- The project uses AWS SDKs, Redis, Mongo, gRPC, etc.
- The project has many dependencies but none match the hard-coded list

As a result, real-world projects may incorrectly report:

> "No external systems or infrastructure dependencies detected."

This is **working as coded**, but **wrong by design**.

---

## Core Insight (Design Correction)

### Libraries ≠ External Systems

Maven dependencies are *libraries*.

What we actually care about is:
- External **systems**
- External **infrastructure dependencies**
- Things that affect **availability, RTO, MTO, blast radius**

Examples:
- Kafka (Messaging)
- Relational DB (Persistence)
- Redis (Cache)
- S3 (Object storage)
- External HTTP APIs

The tool must **infer external systems**, not merely list libraries.

---

## Intended Direction (Agreed Path)

### Heuristic-Based External Dependency Inference

Instead of hard-coded `if` statements, introduce a **rule-driven inference engine**.

The tool should answer:
> "Based on what I see in this repo, what external systems does this service likely depend on?"

Not:
> "Did I see dependency X?"

---

## Proposed Architecture (Next Iteration)

### 1. Rule-Based Classification

Introduce a data-driven rules file (YAML or JSON), e.g.:

```yaml
rules:
  - match:
      maven:
        - spring-kafka
        - kafka-clients
    dependency:
      name: Apache Kafka
      category: Messaging

  - match:
      maven:
        - spring-boot-starter-data-jpa
        - hibernate-core
    dependency:
      name: Relational Database
      category: Persistence
```

The scanner:
1. Extracts Maven coordinates (`groupId`, `artifactId`)
2. Applies rules
3. Emits **classified external dependencies**

---

### 2. Lightweight POM Parsing (No Maven Execution)

Do **not** invoke Maven.

Instead:
- Use simple XML parsing or regex
- Extract `groupId` + `artifactId`
- Ignore versions for now

This is fast, deterministic, and good enough for architecture inference.

---

### 3. Classification Over Enumeration

The report should say:

- "📡 Messaging: Apache Kafka"
- "💾 Persistence: Relational Database"

Not:

- "spring-kafka detected"

This framing aligns with:
- SRE practices
- Production readiness reviews
- Incident impact analysis

---

## Stretch Signals (Future, Not Yet Implemented)

These are **explicitly out of scope for the current iteration**, but define the long-term trajectory:

- Spring config inspection (`application.yml`, `application.properties`)
- Environment variable name heuristics (`KAFKA_`, `DB_`, `REDIS_`)
- Port usage inference
- Confidence levels (High / Medium / Low)
- Dependency → RTO/MTO alignment
- Dependency → CI requirements

---

## Overall Goal (Big Picture)

Create a tool that can answer, from a cold repo checkout:

- What kind of service is this?
- What external systems does it depend on?
- How risky is it operationally?
- What production-readiness gaps are obvious?

Without:
- Running the service
- Deploying it
- Asking the original authors

This is meant to support:
- Architecture reviews
- SRE onboarding
- Incident preparedness
- Due diligence on unfamiliar codebases

---

## Immediate Next Steps (For Gemini 3)

1. Refactor external dependency detection into a **rule engine**
2. Replace hard-coded string checks
3. Parse Maven dependencies generically
4. Emit classified external systems in the report
5. Ensure graceful degradation remains intact

---

## Non-Goals (Important)

- Full dependency resolution
- Vulnerability scanning
- Runtime metrics
- Replacing build tools

This is an **insight and inference tool**, not a build or security scanner.

---

## Final Note to the Next AI

When making decisions:
- Prefer **deterministic heuristics** over LLM guesses
- Prefer **human-readable insight** over raw data
- Optimize for **architecture understanding**, not completeness

If forced to choose:
> Being directionally correct is more valuable than being exhaustively accurate.

