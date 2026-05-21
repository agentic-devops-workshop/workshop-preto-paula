# Tasks: Monthly Payment Cycle Generation

**Feature**: `001-payment-cycle-generation`
**Spec**: [`spec.md`](spec.md) · **Plan**: [`plan.md`](plan.md)

> Granular tasks, dependency-ordered, sized for ~1 PR each. T001-T012 = MVP P1. T013-T020 = P2/P3 + polish.

Legend: `[P1]` MVP · `[P2]` Important · `[P3]` Nice-to-have · `[blocked-by: Tnnn]` dependency

| ID | Task | Priority | Files | DoD | Blocked by |
|---|---|---|---|---|---|
| **T001** | Flyway migration V001 — `payment_cycle`, `payment` (partitioned), `cycle_failure`, `cycle_skip` tables, constraints and indexes | [P1] | `backend/src/main/resources/db/migration/payment/V001__payment_cycle_init.sql` | Migration runs cleanly on empty DB + on DB with existing beneficiary schema; partition `payment_2026` exists; unique constraints verified | — |
| **T002** | Domain value objects — `Money`, `Competence`, enums (`CycleStatus`, `PaymentStatus`, `PaymentType`, `SkipReason`) | [P1] | `domain/Money.java`, `domain/Competence.java`, enums | Unit tests cover boundaries: `Money` truncates `0.0049 → 0.00`; `Competence.parse("2026-13")` throws | — |
| **T003** | Calculation primitives — `MainframeTruncation`, `KFactor`, `RegionalFactor`, `FamilyFactor`, `IncomeFactor`, `AgeFactor`, `ChristmasAllowance` | [P1] | `domain/calculation/*.java` | **100% line coverage** on each; every BR boundary tested; ArchUnit rule "`RoundingMode.DOWN` only here" passes | T002 |
| **T004** | `PaymentFormula` orchestrator + `Payment` aggregate domain class | [P1] | `domain/calculation/PaymentFormula.java`, `domain/Payment.java`, `domain/PaymentCycle.java` | `PaymentFormula.compute(beneficiary, program, competence)` is a pure function; unit test asserts December skips FFAM/FRND; type-A December adds christmas allowance | T003 |
| **T005** | Equivalence fixture + test harness — capture legacy output to `src/test/resources/fixtures/legacy-fixture-2026-05.csv`; write `PaymentCycleEquivalenceTest` | [P1] | `src/test/.../PaymentCycleEquivalenceTest.java`, fixture CSV | SHA-256 of new output equals SHA-256 of legacy capture; diff tool reports row-by-row deltas on mismatch; fixture includes region-99, LEGACY_BACKDOOR, December type-A/P, truncation-edge rows | T004 |
| **T006** | Repositories (Spring Data JPA) + JPA mappings for the 4 tables | [P1] | `infrastructure/persistence/*.java`, `domain/PaymentCycle.java` JPA annotations | Repository integration test on Testcontainers PostgreSQL 16; unique constraint violations surface as `DataIntegrityViolationException` | T001 |
| **T007** | Published ports — `BeneficiarySnapshotPort`, `SocialProgramQueryPort`, `AuditEventPublisher` (interfaces only, no impls yet) | [P1] | `application/ports/*.java` | ArchUnit rule "application depends only on api packages of other contexts" passes | — |
| **T008** | `PaymentCycleService` — trigger, status transitions, idempotency translation | [P1] | `application/PaymentCycleService.java` | Unit tests: trigger creates `Pending`; double trigger → 409 translated from constraint; program inactive → 409; future competence → 400 | T004, T006, T007 |
| **T009** | Spring Batch job — `PaymentCycleJobConfig`, `BeneficiaryItemReader` (JDBC cursor by CPF), `PaymentItemProcessor`, `PaymentItemWriter` (chunk 1000) | [P1] | `infrastructure/batch/*.java` (ADR-005) | `@SpringBatchTest` runs job end-to-end on 100 rows in Testcontainers; rows sorted by CPF (BR-033); restart resumes from last commit | T008 |
| **T010** | REST controller — `POST /payment-cycles`, `GET /payment-cycles/{id}`, `GET /payment-cycles` | [P1] | `interfaces/PaymentCycleController.java`, DTOs | RestAssured tests cover happy path + RBAC + 400 + 404 + 409 + RFC 7807 error body | T008 |
| **T011** | Region-99 audit hook — emit `RegionBypassUsed` event, set `payment.region_bypass = true`, metric `sifap.cycle.region99_bypass.count` | [P1] | processor extension, event class | Equivalence fixture includes region-99 row that produces metric +1 and audit event with severity `WARN` | T009 |
| **T012** | ArchUnit suite — package-by-feature rules + truncation rule + "no JPA in domain" rule | [P1] | `src/test/.../PaymentProcessingArchitectureTest.java` | All 6 rules pass in CI; deliberately failing fixture proves they catch violations | T009 |
| **T013** | Cancel + resume endpoints — `POST /payment-cycles/{id}/cancel`, `POST /payment-cycles/{id}/resume`; `Voided` flip for partial rows | [P2] | `PaymentCycleService` extension, controller | Cancel mid-run flips partial rows to `Voided`; resume re-launches with same JobParameters; idempotent | T010 |
| **T014** | CSV report endpoint + ADR-007 masking — `GET /payment-cycles/{id}/report.csv` with `revealCpf` query param + audit | [P2] | exporter, controller | Default response has masked CPF; `revealCpf=true` requires `ADM` or `AUD` and emits 1 audit event per CPF | T010 |
| **T015** | Quartz scheduler + boot guard — `QuartzCycleScheduler`, `CycleSchedulerBootGuard` (refuses prod auto-trigger without SENARC flag) | [P2] | `infrastructure/scheduler/*.java`, `config/CycleSchedulerBootGuard.java` | Integration test asserts boot fails with `IllegalStateException` when `spring.profiles.active=prod` + `sifap.cycle.scheduler.enabled=true` + no SENARC ticket | T009 |
| **T016** | Observability — Micrometer counters/timers/gauges, structured logs with correlationId, Prometheus endpoint | [P2] | `MetricsConfig`, listener updates | `/actuator/prometheus` exposes `sifap.cycle.*`; log lines have `correlationId`+`cycleId`; manual trace check | T011 |
| **T017** | Progress events every 10s during run — `PaymentCycleStepListener` publishing application event consumed by SSE endpoint | [P3] | `infrastructure/batch/PaymentCycleStepListener.java`, controller | A test client subscribes to SSE and receives ≥1 progress event in a 30s run | T009 |
| **T018** | Performance test — Gatling scenario 100k beneficiaries, asserts ≥ 1000 rows/s sustained | [P2] | `src/test/.../perf/PaymentCyclePerfTest.scala` | Test passes in CI on a 4-vCPU runner; report archived as artifact | T009 |
| **T019** | OWASP ZAP integration in CI — fail build on high/critical findings against the cycle endpoints | [P2] | `.github/workflows/security.yml` extension | ZAP active scan run nightly + on PR touching `paymentprocessing/`; baseline configured | T010 |
| **T020** | Documentation — README for the feature, runbook (cancel/resume/stale recovery), update glossary entries for `PaymentCycle` and `CycleStatus` | [P3] | `specs/001-.../README.md`, `docs/runbook.md`, `01-arqueologia/glossary.md` | Runbook walked through by Par 5; reviewer simulated a Stale recovery and the runbook held up | T013, T015 |

## Acceptance Mapping (FR → Tasks)

| FR | Tasks covering |
|---|---|
| FR-001 | T008, T010 |
| FR-002 | T001 (unique), T008 (409 translation) |
| FR-003 | T008 |
| FR-004 | T009 (BeneficiaryItemReader) |
| FR-005 | T009 (processor returns null) |
| FR-006 | T003, T004, T005 |
| FR-007 | T003 (RegionalFactor) |
| FR-008 | T003 (FamilyFactor) |
| FR-009 | T003 (IncomeFactor) |
| FR-010 | T003 (AgeFactor) |
| FR-011 | T003 (KFactor, MYS-003 magic constant) |
| FR-012 | T003 (MainframeTruncation), T012 (ArchUnit guard) |
| FR-013, FR-014, FR-015 | T003 (ChristmasAllowance), T004 (formula), T005 (fixture) |
| FR-016 | T009 |
| FR-017 | T011, T016 |
| FR-018 | T001, T008 |
| FR-019 | T017 |
| FR-020 | T013 |
| FR-021 | T008 |
| FR-022 | T008 + T010 (forceBackfill role check + audit) |
| FR-023 | T011 (region-99 preservation) |
| FR-024 | T009 (LEGACY_BACKDOOR processed normally) |
| FR-025 | T009 (per-row failure capture) |
| FR-026 | T010 |
| FR-027 | T014 |
| FR-028 | T016 |
| FR-029 | T012 (ArchUnit forbids cross-context writes) |
| FR-030 | T015 |
| FR-EQUIV-* | T005 |
| NFR-PERF-* | T018 |
| NFR-SEC-* | T010, T013, T014 |
| NFR-COMP-* | T001 (immutable audit), T014 (lifecycle) |
| NFR-OBS-* | T016 |

## Out of Scope (not in this feature)

- CNAB 240 bank file generation
- Bank return reconciliation
- Notification to beneficiary
- Tax reporting
