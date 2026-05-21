# Cross-Artifact Consistency Analysis

**Date**: 2026-05-20
**Scope**: `specs/001-payment-cycle-generation/` and `specs/002-beneficiary-registration/`
**Inputs**: `spec.md`, `plan.md`, `tasks.md`, plus `.specify/memory/constitution.md` v1.0.0 and `02-spec-moderna/baseline/`.

## 1. Coverage Matrix — Spec → Plan → Tasks

### 001 Payment Cycle Generation

| Layer | Count | Status |
|---|---|---|
| FRs in spec | 30 (FR-001..FR-030) + 3 FR-EQUIV + 9 NFR + 4 CON | ✅ |
| FRs mapped to tasks | 30/30 (acceptance mapping in `tasks.md`) | ✅ |
| FR-EQUIV mapped | 3/3 → T005 | ✅ |
| NFRs mapped | 9/9 → T015, T016, T018, T001, T010 | ✅ |
| Tasks with no FR | 0 | ✅ |

### 002 Beneficiary Registration

| Layer | Count | Status |
|---|---|---|
| FRs in spec | 25 | ✅ |
| FRs mapped to tasks | 25/25 | ✅ |
| Clarifications resolved | 4/4 (CL-001..CL-004) | ✅ |
| Tasks with no FR | 0 | ✅ |

## 2. Constitution Compliance

| Principle | 001 | 002 |
|---|---|---|
| I — Legacy Traceability (NON-NEGOTIABLE) | ✅ every FR has `source_legacy`; FR-EQUIV-001..003 guarantee byte equivalence | ✅ every FR has `source_legacy`; T013 (CPF equivalence test) covers the legacy boundary |
| II — Modular Monolith (NON-NEGOTIABLE) | ✅ single module `paymentprocessing`; reads via published ports | ✅ single module `beneficiary`; ports defined |
| III — Test-First (NON-NEGOTIABLE) | ✅ plan §8 + tasks T005, T012, T018 | ✅ plan §8 + tasks T012, T013, T016 |
| IV — Stack Discipline | ✅ Java 21 + Spring Boot 3.3 + Spring Batch + Quartz + PostgreSQL 16 | ✅ same stack, no extras |
| V — Single Source of Truth | ✅ formulas live only in `calculation/*`; ArchUnit rule guards | ✅ CPF rules live only in `BrazilianCpfValidator` |
| VI — Security & Compliance | ✅ ZAP gate (T019), audit (T011), retention (T014 CSV via blob lifecycle) | ✅ reveal endpoint audit (T010), log filter (T014), backdoor guard (T008) |
| VII — Observability | ✅ metrics, logs, alerts in plan §9 + T016 | ✅ metrics + alert rule in T015 |

## 3. Cross-Feature Consistency

| Item | 001 | 002 | Aligned? |
|---|---|---|---|
| Beneficiary lifecycle states | reads `Active` only (FR-005) | defines 5 states (CL-002) | ✅ |
| CPF backdoor handling | processes `LEGACY_BACKDOOR` normally (FR-024) | tracks `cpfValidationStatus` (T002) | ✅ |
| Region-99 bypass | preserved (FR-023) | not in scope here | ✅ |
| Source of beneficiary data | via `BeneficiarySnapshotPort` (read-only) | owns the data | ✅ — direction respected |
| Source of program data | via `SocialProgramQueryPort` | via `SocialProgramQueryPort` | ✅ — both go through SocialProgramRegistry port |
| Audit event publication | `AuditEventPublisher` port | `AuditEventPublisher` port | ✅ — both produce events for `ReportingAndAudit` |

## 4. Open Risks / Items Marked `[PENDING-SENARC]`

1. **CL-001 (002)**: `max_dependents` default 5 — pending SENARC written confirmation. Mitigation: configurable per program, no code change required on flip.
2. **CL-002 / CON-004 (001)**: region-99 bypass preserved exactly. Mitigation: feature flag `sifap.cycle.region99.bypass.enabled` ready for future flip.
3. **FR-030 (001)**: Quartz auto-scheduler **disabled by default in production** until SENARC sign-off. Mitigation: `CycleSchedulerBootGuard` refuses boot.

## 5. ADR Coverage

| ADR | Used in 001 | Used in 002 |
|---|---|---|
| ADR-001 Modular Monolith | §1 module layout, ArchUnit | §1 module layout, ArchUnit |
| ADR-002 PE Groups Mapping | n/a | §2 (dependent child table) |
| ADR-005 Batch Orchestration | §5 (Spring Batch + Quartz) | n/a |
| ADR-006 Data Migration Strategy | T011 (LEGACY_BACKDOOR honored) | T013 (equivalence) |
| ADR-007 PII Masking | T014 (CSV mask + reveal) | T003 (masker), T010 (reveal) |
| ADR-008 CPF Test Backdoors | T011 (processed normally) | T008 (boot guard) |

## 6. Test Pyramid Coverage Per Feature

| Test Layer | 001 | 002 |
|---|---|---|
| Domain unit (100%) | T003 calculation primitives | T002, T003 |
| Service unit (≥90%) | T008 cycle service | T006, T007 |
| Repository integration (Testcontainers) | T006 | T005 |
| API integration (RestAssured) | T010, T013 | T009, T010 |
| ArchUnit | T012 (incl. truncation guard) | T012 |
| Boot guard | T015 (scheduler) | T008 (CPF backdoor) |
| Equivalence | T005 (SHA-256 vs legacy fixture) | T013 (10k CPF parity) |
| Performance | T018 (Gatling) | T016 (Gatling) |
| Security | T019 (ZAP) | T017 (ZAP) |

## 7. Anti-Patterns Checked (and absent)

- ❌ Microservices: not present — modular monolith honored.
- ❌ Business logic in stored procedures: all logic in Java.
- ❌ `RoundingMode.HALF_UP` in monetary code: forbidden by ArchUnit rule (001 T012).
- ❌ Cross-context direct SQL: blocked by ArchUnit + ports.
- ❌ Field injection: plan §6/§7 use constructor injection.
- ❌ Raw CPF in logs: filter (002 T014) + grep CI gate.
- ❌ Spec without `source_legacy`: zero occurrences (verified by counting).
- ❌ "Done after the fact" tests: T001 contains migration before T002 entities; T005 fixture before T009 batch.

## 8. Verdict

Both specs are **READY FOR IMPLEMENTATION**. No blocking inconsistencies. Three items remain `[PENDING-SENARC]` but each has a documented mitigation that allows implementation to proceed without breaking change later.

## 9. Recommended Sequence (workshop scope)

For the workshop's 8-hour budget, the recommended slice is the union of P1 tasks from both features, in this order:

1. 002 T001 + T002 + T003 (beneficiary schema and CPF primitives) — unlocks 001 reads.
2. 002 T004 + T005 + T006 + T007 (beneficiary entities, repositories, services).
3. 002 T008 + T009 + T010 (controllers, reveal, backdoor guard).
4. 001 T001 + T002 + T003 (payment schema and calculation primitives).
5. 001 T004 + T005 (formula + equivalence harness — the showcase moment).
6. 001 T006 + T007 + T008 + T009 (repos, ports, service, batch job).
7. 001 T010 + T011 + T012 (controller, region-99 audit, ArchUnit).
8. Both features T012 (ArchUnit) + T014/T013 (equivalence) — gates green.

Demo focal point: trigger 001 cycle on a fixture, show CPF masking in the CSV, prove SHA equivalence against legacy.
