# Tasks: Beneficiary Registration

**Feature**: `002-beneficiary-registration`
**Spec**: [`spec.md`](spec.md) · **Plan**: [`plan.md`](plan.md)

Legend: `[P1]` MVP · `[P2]` Important · `[P3]` Nice-to-have

| ID | Task | Priority | Files | DoD | Blocked by |
|---|---|---|---|---|---|
| **T001** | Flyway V001 — `beneficiary`, `dependent` tables, indexes, constraints, comments | [P1] | `db/migration/beneficiary/V001__beneficiary_module_init.sql` | Migration green on empty DB; constraints fire on bad data; soft-delete column `cancelled_at` present | — |
| **T002** | Domain value objects — `Cpf`, `Address`, enums (`LifecycleStatus`, `AgeCategory`, `Parentage`, `CpfValidationStatus`) | [P1] | `domain/Cpf.java`, `domain/Address.java`, enums | Unit tests for `Cpf.format` masking pattern, equals/hashCode, `LifecycleStatus.canTransitionTo` per CL-002 matrix | — |
| **T003** | `BrazilianCpfValidator` + `BrazilianCpfMasker` (ADR-007, ADR-008) | [P1] | `domain/BrazilianCpfValidator.java`, `domain/BrazilianCpfMasker.java` | 100% coverage; strict Mod-11; backdoor list rejected in prod; masker output `XXX.XXX.XXX-NN` | T002 |
| **T004** | `Beneficiary` + `Dependent` JPA entities; `domain/events/*` event classes | [P1] | `domain/Beneficiary.java`, `domain/Dependent.java`, `domain/events/*.java` | Entity round-trip via Testcontainers passes; `@DomainEvents` returns proper events on save | T002 |
| **T005** | Repositories — `BeneficiaryRepository`, `DependentRepository`, `BeneficiaryQueryRepository` | [P1] | `infrastructure/persistence/*.java` | Integration tests: find by CPF, find by NIS, unique constraint violation surfaces correctly | T001, T004 |
| **T006** | `BeneficiaryService` — register, update, change lifecycle status (CL-002 state machine), soft-delete | [P1] | `application/BeneficiaryService.java` | Unit tests cover happy path + invalid transition + dup CPF; transaction boundaries on `@Transactional` only here | T003, T004, T005 |
| **T007** | `DependentService` — add, list, remove dependents; enforce max-5 (CL-001) and titular-not-active block | [P1] | `application/DependentService.java` | Tests: add succeeds; 6th dependent → 409; titular Suspended → 409; dep CPF=null skips uniqueness | T006 |
| **T008** | `CpfBackdoorBootGuard` — `@PostConstruct` refuses prod with `sifap.cpf.allowTestCpfs=true` | [P1] | `infrastructure/config/CpfBackdoorBootGuard.java` | `@SpringBootTest(properties={...prod, flag=true})` MUST fail to start | T003 |
| **T009** | REST controllers — `BeneficiaryController`, `DependentController`, DTOs (CPF masked in responses) | [P1] | `interfaces/*.java`, `interfaces/dto/*.java` | RestAssured covers all 11 endpoints in plan §3 + RBAC + 400/401/403/404/409 + RFC 7807 errors | T006, T007 |
| **T010** | `CpfRevealService` + reveal endpoint — `POST /beneficiaries/{cpf}/reveal-cpf` with mandatory `purpose`; emits audit event | [P1] | `application/CpfRevealService.java`, controller method | Test: missing purpose → 400; revealed value returned in plaintext; 1 audit event emitted | T009 |
| **T011** | `AgeCategoryRecomputeJob` — Spring `@Scheduled` daily 02:00 America/Sao_Paulo (CL-003); idempotent | [P2] | `application/AgeCategoryRecomputeJob.java` | Test triggers job manually; rows with crossed birthday are updated; metric `sifap.beneficiary.ageCategory.updated` increments | T006 |
| **T012** | ArchUnit suite — package boundaries + domain framework-free rule + cross-context import guards | [P1] | `src/test/.../BeneficiaryArchitectureTest.java` | 6 rules pass; deliberately failing fixture proves they fail-build | T009 |
| **T013** | Equivalence harness vs `VALBENEF.NSN` — 10,000 random CPFs accept/reject decision matches the Java port | [P1] | `src/test/.../CpfEquivalenceTest.java`, fixture | All 10,000 decisions match | T003 |
| **T014** | Log filter — `MaskedCpfLogFilter` + grep CI gate ensuring no 11-digit CPF appears in logs | [P1] | `infrastructure/log/MaskedCpfLogFilter.java`, CI step | CI step greps build logs for raw CPF and fails if found | T003 |
| **T015** | Observability — counters for registered/rejected/revealed/backdoor_accepted; Prometheus exposure; alert rule for `cpf_backdoor.accepted > 0 in prod` | [P2] | `MetricsConfig`, alert YAML | `/actuator/prometheus` shows the 4 counters; alert rule committed | T009 |
| **T016** | Performance test — 100 RPS on `GET /beneficiaries/{cpf}` with P95 ≤ 300ms | [P2] | `src/test/.../perf/BeneficiaryPerfTest.scala` | Test green on 4-vCPU runner | T009 |
| **T017** | OWASP ZAP scan against the beneficiary endpoints in CI | [P2] | `.github/workflows/security.yml` extension | 0 high/critical findings; reveal endpoint requires token | T009 |
| **T018** | Documentation — README for the feature; glossary entries for `Beneficiary`, `Dependent`, `LifecycleStatus`, `CpfValidationStatus`; runbook for reveal-CPF abuse incident | [P3] | `specs/002-.../README.md`, `01-arqueologia/glossary.md`, `docs/runbook.md` | Par 5 walks the runbook; glossary cross-links open | T010 |

## Acceptance Mapping (FR → Tasks)

| FR | Tasks |
|---|---|
| FR-001 to FR-007 (register) | T002, T003, T004, T006, T009 |
| FR-008 to FR-013 (dependents) | T002, T007, T009 |
| FR-014 to FR-016 (read) | T005, T009 |
| FR-017 to FR-019 (update) | T006, T009 |
| FR-020 to FR-022 (lifecycle, soft-delete) | T006, T009 |
| FR-023 (reveal CPF) | T010 |
| FR-024 (age recompute) | T011 |
| FR-025 (legacy backdoor immutability) | T006, T013 |
| NFR-PERF-* | T016 |
| NFR-SEC-* | T008, T010, T014, T017 |
| NFR-COMP-* | T010, T015 |
| NFR-OBS-* | T015, T014 |
