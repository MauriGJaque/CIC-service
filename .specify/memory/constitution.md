<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0
Modified principles: N/A (initial ratification from template)

Added sections:
  - I. Clean Architecture (Robert Martin)
  - II. BDD Testing Strategy
  - III. Programming Best Practices (SOLID, YAGNI, DRY)
  - IV. API First Design
  - V. Quality Metrics & Coverage
  - Code Quality Standards
  - Development Workflow

Templates updated:
  ✅ .specify/memory/constitution.md (this file)
  ⚠ .specify/templates/plan-template.md — Constitution Check section references these 5 gates; update manually if needed
  ⚠ .specify/templates/spec-template.md — User stories should include BDD Given/When/Then scenarios (already aligned)
  ⚠ .specify/templates/tasks-template.md — Test tasks should specify unit/integration/functional per BDD; update if needed

Follow-up TODOs:
  - TODO(RATIFICATION_DATE): Confirm exact project start date if different from 2026-07-02
-->

# CIC-service Constitution

## Core Principles

### I. Clean Architecture (NON-NEGOTIABLE)

The project MUST follow Robert C. Martin's Clean Architecture:

- **Dependency Rule**: Source code dependencies MUST only point inward. Outer layers (frameworks, UI, DB) MUST depend on inner layers (entities, use cases), never the reverse.
- **Layer separation** (outer → inner): Frameworks & Drivers → Interface Adapters → Application Use Cases → Domain Entities.
- **Domain Entities**: Pure business objects with no dependency on frameworks, databases, or external systems.
- **Use Cases**: Application-specific business rules. MUST be independently testable without any infrastructure concern.
- **Interface Adapters**: Controllers, presenters, gateways. Convert data between use cases and external formats.
- **Infrastructure**: Spring Boot, JPA, HTTP clients, message brokers — isolated in the outermost layer only.
- No framework annotation (e.g., `@Service`, `@Repository`) MUST appear inside domain or use-case classes.
- Package structure MUST reflect layers: `domain/`, `application/`, `adapters/`, `infrastructure/`.

**Rationale**: Ensures the business logic is portable, independently testable, and protected from framework churn.

### II. BDD Testing Strategy (NON-NEGOTIABLE)

All tests MUST be structured using Behavior-Driven Development (BDD):

- **Unit tests**: Test a single class/use case in isolation. MUST use Given/When/Then naming convention. Mock all collaborators.
- **Integration tests**: Test interaction between layers (e.g., repository + DB, adapter + use case). MUST use Spring context slices.
- **Functional tests**: End-to-end scenarios covering full HTTP request→response cycles. MUST reflect real user scenarios.
- Every test MUST follow the `@Given / @When / @Then` structure either via comments or JUnit 5 `@DisplayName` labels.
- Test class names MUST end in `Test` (unit/integration) or `FunctionalTest` (functional).
- Tests MUST be written BEFORE the implementation (TDD/BDD red-green-refactor cycle).
- Test files MUST reside under:
  - `src/test/java/.../unit/` — unit tests
  - `src/test/java/.../integration/` — integration tests
  - `src/test/java/.../functional/` — functional tests

**Rationale**: BDD bridges business requirements and technical tests, making test failures self-explanatory and acceptance criteria directly verifiable.

### III. Programming Best Practices (NON-NEGOTIABLE)

All code MUST comply with the following principles:

- **SOLID**:
  - *Single Responsibility*: Each class/module MUST have one reason to change.
  - *Open/Closed*: Classes MUST be open for extension and closed for modification.
  - *Liskov Substitution*: Subtypes MUST be substitutable for their base types without altering correctness.
  - *Interface Segregation*: Interfaces MUST be narrow; clients MUST NOT depend on methods they do not use.
  - *Dependency Inversion*: High-level modules MUST NOT depend on low-level modules; both MUST depend on abstractions.
- **YAGNI** (You Aren't Gonna Need It): Implement only what is explicitly required. No speculative features allowed.
- **DRY** (Don't Repeat Yourself): Every piece of knowledge MUST have a single, unambiguous authoritative representation.
- Methods longer than 30 lines MUST be reviewed for decomposition.
- Classes with more than 10 public methods SHOULD be split.

**Rationale**: These principles produce maintainable, extensible, and readable code and prevent accidental complexity.

### IV. API First Design (NON-NEGOTIABLE)

All APIs MUST follow the API First approach:

- An **OpenAPI 3.x contract** (YAML or JSON) MUST exist and be approved BEFORE any implementation begins.
- Contracts MUST reside in `src/main/resources/openapi/` or `docs/api/`.
- **openapi-generator** MUST be used to generate server stubs and client models from the contract. No hand-written API boilerplate.
- Generated code MUST NOT be modified manually; changes go through the OpenAPI contract only.
- API versioning MUST be expressed in the URL path (e.g., `/api/v1/`).
- Every endpoint MUST declare: request schema, response schema, error responses (4xx/5xx), and security requirements.
- Breaking changes to a contract MUST trigger a major version bump of the API.
- Contracts MUST be linted (e.g., Spectral) as part of the CI pipeline.

**Rationale**: API First decouples consumer and producer teams, enables parallel development, and produces living documentation.

### V. Quality Metrics & Coverage (NON-NEGOTIABLE)

Code coverage MUST meet the following thresholds, enforced by **JaCoCo**:

- **Per-class coverage**: MUST be > 80% (line + branch).
- **Global project coverage**: MUST be ≥ 80% (line + branch).
- JaCoCo MUST be configured to **fail the build** if thresholds are not met.
- JaCoCo reports MUST be generated on every build: `target/site/jacoco/index.html`.
- Coverage exclusions (e.g., generated code, DTOs) MUST be explicitly declared in the JaCoCo configuration and justified in a comment.
- Coverage metrics MUST be reviewed in every pull request.
- Mutation testing (e.g., PIT) is RECOMMENDED for critical domain classes.

**Rationale**: Coverage gates prevent regressions and ensure meaningful test suites; JaCoCo provides actionable, build-integrated visibility.

## Code Quality Standards

- All code MUST pass static analysis (e.g., Checkstyle, SpotBugs, PMD) with zero high-severity violations in CI.
- Code MUST be formatted with a project-wide formatter config (e.g., Google Java Format or project `.editorconfig`).
- No `System.out.println` in production code; use SLF4J/Logback structured logging.
- Exceptions MUST be specific: no bare `catch (Exception e)` in domain or application layers.
- All public API methods in domain and application layers MUST have non-trivial Javadoc explaining the *why*, not the *what*.
- Dependencies MUST be managed via Maven/Gradle BOM or version catalog to avoid version conflicts.

## Development Workflow

- All features MUST start with a spec (`/speckit-specify`) → plan (`/speckit-plan`) → tasks (`/speckit-tasks`) before coding.
- The OpenAPI contract MUST be created and reviewed as part of the plan phase (Phase 1 deliverable).
- BDD test scenarios (from spec.md) MUST map 1:1 to test methods.
- A feature MUST NOT merge unless: all tests pass, coverage gates pass, OpenAPI contract is present, and static analysis is clean.
- Pull requests MUST reference the related spec and include a link to the JaCoCo coverage report.
- Architecture violations MUST be detected via ArchUnit tests in CI.

## Governance

- This constitution supersedes all other coding guidelines and conventions within this project.
- Amendments MUST be proposed as a pull request modifying this file with an updated Sync Impact Report.
- Amendments require approval from at least one tech lead and re-execution of `/speckit-constitution`.
- Each amendment MUST increment the version following semantic versioning:
  - **MAJOR**: Removal or redefinition of a non-negotiable principle.
  - **MINOR**: New principle, section, or materially expanded guidance.
  - **PATCH**: Wording clarifications, typo fixes, non-semantic refinements.
- Compliance MUST be verified in every PR and enforced by CI gates (coverage, linting, architecture tests).
- This constitution is reviewed every quarter or after any major incident.

**Version**: 1.0.0 | **Ratified**: 2026-07-02 | **Last Amended**: 2026-07-02
