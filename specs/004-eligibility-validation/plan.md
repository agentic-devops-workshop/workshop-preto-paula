# Implementation Plan: Eligibility Validation

**Branch**: `004-eligibility-validation` | **Date**: 2026-05-20 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-eligibility-validation/spec.md`

## Summary

Replace the legacy `VALELEG.NSN` eligibility validator with a pure, testable rule engine that (a) enforces the three program-type rules (A income+dependents per BR-025, P age ≥ 60 per BR-026, T age 16–65 per BR-027), (b) preserves the region-99 bypass (BR-024 / MYS-008) under explicit audit, (c) refuses to start in production when the bypass is disabled but legacy region-99 rows still exist, and (d) exposes a published port `BeneficiaryEligibilityPort` consumed by features 002 (Beneficiary Registration) and 001 (Payment Cycle). Single in-process module within the modular monolith.

## Technical Context

**Language/Version**: Java 21 LTS (Constitution Principle IV)

**Primary Dependencies**: Spring Boot 3.3, Spring Web, Spring Data JPA (read-only access for the region-99 startup probe — the validator itself is dependency-free), Jakarta Validation, Micrometer, springdoc-openapi

**Storage**: PostgreSQL 16 (read-only against existing `beneficiary` and `social_program` tables owned by features 002 and 003; no new tables in this feature)

**Testing**: JUnit 5, AssertJ, jqwik (property-based tests), ArchUnit, RestAssured, Spring `@SpringBootTest` for boot-guard integration

**Target Platform**: Linux container on AKS (this feature is a package inside the modular monolith, not a separate deployable)

**Project Type**: Web application — Java backend; the operator UI for the region-99 report ships in feature 011

**Performance Goals**: P95 ≤ 5 ms per `validate()` call; ≥ 100 000 validations/sec sustained (covers 4.2 M-row cycle within the 70-minute budget set by 001 NFR-PERF-001); region-99 report query P95 ≤ 1 s per cycle

**Constraints**: Pure function (no per-call DB round-trip); cross-context access only via published `api/` package (ArchUnit-enforced); region-99 short-circuit preserved exactly; **NEEDS CLARIFICATION** — snapshot semantics across program updates mid-cycle (open clarification Q2 from `/speckit.clarify`)

**Scale/Scope**: ~30 active programs × 4.2 M beneficiaries = ~4.2 M validations per monthly cycle, plus ~10 k/day from registrations and ~1 k/day from the simulate UX path

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Principle | Status | Evidence |
|---|---|---|
| I — Legacy Traceability (NON-NEGOTIABLE) | ✅ PASS | All 12 FRs cite `VALELEG.NSN` line ranges or `[GREENFIELD]` with one-line justification |
| II — Modular Monolith (NON-NEGOTIABLE) | ✅ PASS | Single Spring Boot package `com.sifap.eligibility`; cross-context contact only via `api/BeneficiaryEligibilityPort` and `application/ports/ProgramCriteriaPort` — ArchUnit gate in tasks.md T011 |
| III — Test-First (NON-NEGOTIABLE) | ✅ PASS | Tasks T002 (validator unit, 100% coverage), T003 (property tests), T004 (port contract), T008 (boot guard), T012 (microbenchmark) all written alongside implementation; equivalence fixture format mirrors feature 001 |
| IV — Stack Discipline | ✅ PASS | Java 21 + Spring Boot 3.3 + Micrometer + ArchUnit — all already declared in `pom.xml`. No new dependencies. |
| V — Single Source of Truth | ✅ PASS | `EligibilityValidator` is the only encoder of BR-024..BR-027; spec/plan/tasks link to constitution and `business-rules-catalog.md` instead of duplicating |
| VI — Security & Compliance by Construction | ✅ PASS | NFR-SEC-001/002, FR-006 (audit on bypass), FR-007 (boot guard); CPF mask via ADR-007; no new secrets |
| VII — Observability and Operational Honesty | ✅ PASS | NFR-OBS-001 counters `sifap.eligibility.*`, NFR-OBS-002 anomaly alert; correlation-ID propagation inherited from `GlobalExceptionHandler` |

**Verdict**: PASS. No principle violated; complexity tracking table left empty by design.

**Re-check after Phase 1**: ✅ PASS (no new dependencies introduced; data model is read-only against tables owned by 002 and 003).

## Project Structure

### Documentation (this feature)

```text
specs/004-eligibility-validation/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── eligibility-port.contract.md
│   └── eligibility-api.openapi.yaml
├── spec.md
└── tasks.md
```

### Source Code (repository root)

The workshop uses **Option 2 — Web application** structure, with the Java backend under `03-implementacao/backend/`. This feature contributes a single package under `com.sifap.eligibility`:

```text
03-implementacao/backend/
├── src/main/java/com/sifap/eligibility/
│   ├── domain/
│   │   ├── EligibilityValidator.java
│   │   ├── EligibilityResult.java
│   │   ├── EligibilityCriteria.java
│   │   └── Reason.java
│   ├── application/
│   │   ├── EligibilityService.java
│   │   └── ports/
│   │       └── ProgramCriteriaPort.java
│   ├── infrastructure/
│   │   ├── DefaultEligibilityPortAdapter.java
│   │   └── config/
│   │       └── Region99StartupGuard.java
│   ├── interfaces/
│   │   ├── EligibilityController.java
│   │   ├── EligibilityExceptionHandler.java
│   │   └── dto/...
│   └── api/
│       └── BeneficiaryEligibilityPort.java
└── src/test/java/com/sifap/eligibility/
    ├── domain/EligibilityValidatorTest.java
    ├── domain/EligibilityValidatorPropertyTest.java
    ├── interfaces/EligibilityControllerTest.java
    └── architecture/EligibilityArchitectureTest.java
```

**Structure Decision**: Option 2 (Web application). This feature is a single package inside the existing monolith — no new project, no new repository. The operator UI for the region-99 report ships in feature 011 and consumes the REST endpoints from this feature over HTTP.

## Complexity Tracking

No constitution violations identified. Table intentionally left empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |
