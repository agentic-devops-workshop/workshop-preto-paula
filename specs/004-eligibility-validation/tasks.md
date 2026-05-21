---

description: "Task list for feature 004 — Eligibility Validation"
---

# Tasks: Eligibility Validation

**Input**: Design documents from `/specs/004-eligibility-validation/`

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Test tasks are REQUIRED — Constitution Principle III makes Test-First NON-NEGOTIABLE for this workshop. Tests must be written and FAIL before the matching implementation passes.

**Organization**: Tasks are grouped by user story (US1–US4 from spec.md) so each can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel — different files, no incomplete-task dependencies
- **[Story]**: `[US1]`/`[US2]`/`[US3]`/`[US4]` from spec.md; setup/foundation/polish carry no story label
- Paths assume **Web app** structure (plan.md §Project Structure): `03-implementacao/backend/src/{main,test}/java/com/sifap/eligibility/`

---

## Phase 1: Setup

**Purpose**: Wire the empty module skeleton into the existing monolith.

- [x] T001 Create the package skeleton `03-implementacao/backend/src/main/java/com/sifap/eligibility/{domain,application/ports,infrastructure/config,interfaces/dto,api}/` and the matching test root `src/test/java/com/sifap/eligibility/{domain,interfaces,architecture}/`
- [x] T002 [P] Add `net.jqwik:jqwik:1.9.0` as a `test`-scope dependency in `03-implementacao/backend/pom.xml` (research R-006)
- [x] T003 [P] Register the module in `application.yml` under `sifap.eligibility.*` with defaults: `region99Bypass.enabled=true`, `simulate.rateLimit.perMinute=30`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain primitives that every story consumes. Must be complete before any user story phase starts.

- [x] T004 [P] Create enum `Reason` with the 7 documented values in `03-implementacao/backend/src/main/java/com/sifap/eligibility/domain/Reason.java` (data-model.md §`Reason`)
- [x] T005 [P] Create sealed interface `EligibilityResult` with records `Eligible`, `EligibleByBypass(short regionCode)`, `Ineligible(Reason reason, String detail)` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/domain/EligibilityResult.java` (data-model.md §`EligibilityResult`, research R-003)
- [x] T006 [P] Create record `EligibilityCriteria(String programCode, char type, BigDecimal incomeMax, boolean active, boolean retired)` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/domain/EligibilityCriteria.java`
- [x] T007 [P] Create published port interface `BeneficiaryEligibilityPort` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/api/BeneficiaryEligibilityPort.java` matching the contract in `specs/004-eligibility-validation/contracts/eligibility-port.contract.md`
- [x] T008 [P] Create read-only port `ProgramCriteriaPort` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/application/ports/ProgramCriteriaPort.java` with method `Optional<EligibilityCriteria> findByCode(String programCode)`
- [x] T008b [P] Implement the adapter `SocialProgramCriteriaAdapter` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/infrastructure/SocialProgramCriteriaAdapter.java` — wraps feature 003's `SocialProgramQueryPort`, fetches the program, then queries the `social_program` table for `income_max` (a column owned by 003 but not yet on its published port) and assembles the local `EligibilityCriteria` record. Document the mapping `SocialProgramQueryPort.ProgramView → EligibilityCriteria` in a Javadoc citing finding F2 from the analyze report.

**Checkpoint**: All four user-story phases can now begin in parallel.

---

## Phase 3: User Story 1 — Beneficiary registration calls the validator (Priority: P1) 🎯 MVP

**Goal**: A registration in program `BFA1` (type A) blocks with `Ineligible(INCOME_AND_NO_DEPENDENTS)` when income > max and dependents = 0, and accepts otherwise. Region-99 bypass overrides everything.

**Independent Test**: Run the quickstart §3 curl examples — `Eligible`, `Ineligible(INCOME_AND_NO_DEPENDENTS)`, and `EligibleByBypass(99)` outcomes appear without any other user story being implemented.

### Tests for User Story 1

> Write these FIRST. Run them — they MUST fail before implementation lands.

- [x] T009 [P] [US1] Validator unit tests in `03-implementacao/backend/src/test/java/com/sifap/eligibility/domain/EligibilityValidatorTest.java` — 13 cases covering type A/P/T, age boundaries 16/60/65, region-99 bypass, retired program, null/negative input (spec.md US1 Acceptance Scenarios 1–4 + Edge Cases)
- [x] T010 [P] [US1] Property test in `03-implementacao/backend/src/test/java/com/sifap/eligibility/domain/EligibilityValidatorPropertyTest.java` using jqwik — invariant: `regionCode == 99 ⇒ result.isBypass()`; invariant: type-A `Ineligible` ⇒ income > max AND dependents == 0 (research R-006)

### Implementation for User Story 1

- [x] T011 [US1] Implement pure function `EligibilityValidator.validate(criteria, birthDate, income, dependents, regionCode, referenceDate)` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/domain/EligibilityValidator.java` — order: input validation → region-99 short-circuit → program presence/retired → type dispatch (research R-001, R-003, R-004)
- [x] T012 [US1] Implement `DefaultEligibilityPortAdapter` (component) in `03-implementacao/backend/src/main/java/com/sifap/eligibility/infrastructure/DefaultEligibilityPortAdapter.java` — calls validator, emits `RegionBypassEvaluated` via `ApplicationEventPublisher` at WARN on bypass (research R-005)
- [ ] T013 [US1] [CROSS-BRANCH] Wire the port into the `beneficiary` module on the existing branch `002-beneficiary-registration`: `BeneficiaryService.register(...)` calls `BeneficiaryEligibilityPort.validate(...)` and translates `Ineligible` to 422 with RFC 7807 body (spec.md FR-012). **Open as a separate PR against branch `002-beneficiary-registration` after feature 004 merges to `develop`.**

**Checkpoint**: US1 is independently usable — registration enforces eligibility, region-99 bypass works, audit event fires.

---

## Phase 4: User Story 2 — Payment cycle uses the validator (Priority: P1)

**Goal**: A running cycle calls the validator per beneficiary, skips `Ineligible` ones into `cycle_skip`, and processes `EligibleByBypass` ones with the existing `RegionBypassUsed` event from feature 001.

**Independent Test**: Run a cycle of 10 beneficiaries — 8 Eligible, 1 Ineligible (high income, no deps), 1 region-99. Expect 9 payment rows, 1 cycle_skip row, region99_bypass_count = 1, one `RegionBypassEvaluated` + one `RegionBypassUsed` event.

### Tests for User Story 2

- [x] T014 [P] [US2] Integration test in `03-implementacao/backend/src/test/java/com/sifap/eligibility/interfaces/CyclePathIntegrationTest.java` — Spring `@SpringBootTest` + Testcontainers; runs a 10-row cycle with mocked `ProgramCriteriaPort`; asserts the skip + bypass counts (spec.md US2 Acceptance Scenarios 1–2)

### Implementation for User Story 2

- [ ] T015 [US2] [CROSS-BRANCH] Update `PaymentItemProcessor` on branch `001-payment-cycle-generation` in `03-implementacao/backend/src/main/java/com/sifap/paymentprocessing/infrastructure/batch/PaymentCycleJobConfig.java` — replace the inline "Active only" filter with a call to `BeneficiaryEligibilityPort.validate(...)` using `cycle.snapshotAt` as referenceDate; map `Ineligible` to `CycleSkip` row (research R-001). **Program data MUST be resolved once at `cycle.startedAt` and reused for every row (Q2 → A: snapshot at cycle start). Open as a separate PR against branch `001-payment-cycle-generation` after feature 004 merges to `develop`.**
- [x] T016 [US2] Ensure both audit events (`RegionBypassEvaluated` from 004 + `RegionBypassUsed` from 001) coexist without double-counting — single Spring `@EventListener` test asserts each event lands exactly once per region-99 row (research R-005)

**Checkpoint**: US1 and US2 both pass independently; a complete cycle now flows through the validator.

---

## Phase 5: User Story 3 — Admin reviews region-99 usage (Priority: P1)

**Goal**: SENARC analyst gets a per-cycle report of who used region-99, with masked CPF by default and audited reveal.

**Independent Test**: `GET /api/v1/eligibility/region-99-report?cycleId=42` returns the count and the masked-CPF list; `?revealCpf=true` requires `AUD` role and emits one audit event per row.

### Tests for User Story 3

- [x] T017 [P] [US3] Controller test `EligibilityControllerTest.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/interfaces/EligibilityControllerTest.java` — RestAssured covers 200 with mask + 200 reveal flow + 403 without `AUD` + 400 missing params (spec.md US3 + contracts/eligibility-api.openapi.yaml)
- [x] T018 [P] [US3] Service test `EligibilityServiceTest.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/application/EligibilityServiceTest.java` — aggregation pulls `payment.region_bypass = true` rows (research R-008) with mocked repository

### Implementation for User Story 3

- [x] T019 [US3] Implement `EligibilityService.region99Report(cycleId)` and `region99Report(competence, programCode)` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/application/EligibilityService.java` — reads from `payment` table when cycleId is present; pulls `beneficiary` when querying a future competence (research R-008)
- [x] T020 [US3] Implement `Region99ReportDto` and `Region99BeneficiaryDto` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/interfaces/dto/` matching the OpenAPI `Region99Report` schema; default CPF masking via `BrazilianCpfMasker` (ADR-007)
- [x] T021 [US3] Implement `EligibilityController.region99Report(...)` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/interfaces/EligibilityController.java` with `@PreAuthorize("hasAnyRole('ADM','AUD')")`; reveal path emits one `CpfRevealed` audit event per row
- [x] T022 [US3] Implement `EligibilityExceptionHandler` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/interfaces/EligibilityExceptionHandler.java` — RFC 7807 responses for 400 (validation), 403 (RBAC), 404 (unknown cycleId)

**Checkpoint**: US1, US2, and US3 all pass independently; reconciliation of report count with `payment.region_bypass` count holds (Success Criterion #1).

---

## Phase 6: User Story 4 — Operator simulates an eligibility check (Priority: P2)

**Goal**: Registration UI can pre-check eligibility before submit. No persistence, fast response.

**Independent Test**: `POST /api/v1/eligibility/simulate {programCode, birthDate, familyIncome, dependents, regionCode}` returns `{outcome, reason?, detail?}` without writing any row.

### Tests for User Story 4

- [x] T023 [P] [US4] Controller test in `03-implementacao/backend/src/test/java/com/sifap/eligibility/interfaces/SimulateControllerTest.java` — happy path + invalid input + 401 anonymous + 429 over rate limit (spec.md US4, FR-009)

### Implementation for User Story 4

- [x] T024 [US4] Implement `EligibilityController.simulate(...)` in the controller from T021 — maps request to the published port; returns `EligibilityResultDto` matching the OpenAPI schema
- [x] T025 [US4] Implement `EligibilityService.simulate(...)` — pulls `EligibilityCriteria` via `ProgramCriteriaPort`, calls validator with `LocalDate.now()` as referenceDate (research R-001)
- [x] T026 [US4] Configure rate limiter (Bucket4j) on `/simulate` keyed by JWT `sub` (FR-009 — resolved Q3 in `/speckit.clarify`): **30 calls/min per authenticated user**, HTTP 429 + RFC 7807 body when exceeded. Audit policy is shared with FR-006: only `EligibleByBypass` outcomes emit the `RegionBypassEvaluated` event — no extra wiring needed since `DefaultEligibilityPortAdapter` (T012) already handles that.

**Checkpoint**: All four user stories pass independently.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Workshop gates that span every user story.

- [x] T027 [P] ArchUnit suite `EligibilityArchitectureTest.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/architecture/EligibilityArchitectureTest.java` — only `com.sifap.eligibility.api..` may be imported by other modules; `..domain..` must not import any persistence or Spring annotation (Constitution Principle II, plan.md §1)
- [x] T028 Region-99 boot guard `Region99StartupGuard` (`@Profile("prod")`) in `03-implementacao/backend/src/main/java/com/sifap/eligibility/infrastructure/config/Region99StartupGuard.java` — `@PostConstruct` queries `beneficiary` for `region_code = 99` and fails boot when bypass disabled + count > 0 (spec.md FR-007, research R-007)
- [x] T029 Integration test `Region99StartupGuardTest.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/architecture/Region99StartupGuardTest.java` — `@SpringBootTest(properties = {"spring.profiles.active=prod","sifap.eligibility.region99Bypass.enabled=false"})` plus a seeded region-99 row → boot MUST fail
- [x] T030 [P] Observability — register Micrometer counters `sifap.eligibility.evaluated.count{result}`, `sifap.eligibility.region99.count{program}` in `03-implementacao/backend/src/main/java/com/sifap/eligibility/infrastructure/MetricsConfig.java`; assert presence via `/actuator/prometheus` smoke test (spec.md NFR-OBS-001)
- [x] T031 [P] Microbenchmark `EligibilityValidatorBench.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/performance/EligibilityValidatorBench.java` — 100 000 calls in ≤ 1 s on the workshop CI runner (spec.md NFR-PERF-001)
- [x] T032 Equivalence test `EligibilityEquivalenceTest.java` in `03-implementacao/backend/src/test/java/com/sifap/eligibility/domain/EligibilityEquivalenceTest.java` — parameterized over `src/test/resources/fixtures/legacy-fixture-2026-05.csv` (shared with feature 001); asserts decision matches captured legacy output (Constitution Principle I)
- [ ] T033 [P] Quickstart validation — execute every curl in `specs/004-eligibility-validation/quickstart.md` end-to-end against the running app; record outputs in `quickstart-evidence.md` (not committed; PR comment only)
- [x] T034 [P] Documentation — glossary entries for `EligibilityResult`, `Reason`, `Region 99 Bypass` in `01-arqueologia/glossary.md` (cross-link MYS-008); short README at `specs/004-eligibility-validation/README.md` summarizing the feature for newcomers

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** → no deps, starts immediately
- **Foundational (Phase 2)** → depends on Setup; blocks every user story
- **US1 / US2 / US3 / US4 (Phases 3–6)** → all depend on Foundational only; can be staffed in parallel
- **Polish (Phase 7)** → depends on all four user-story phases

### Cross-feature dependencies

- T013 modifies a file on branch `002-beneficiary-registration` — opens a PR against that branch.
- T015 modifies a file on branch `001-payment-cycle-generation` — opens a separate PR against that branch.
- T032 requires the `legacy-fixture-2026-05.csv` from branch `001-payment-cycle-generation` to be on `develop` first.

### Within each user story

- Tests (T009/T010, T014, T017/T018, T023) MUST be written and run first, and MUST fail before the matching implementation lands.
- Models / records → services → controllers in that order.
- A user story is "done" only when its independent test from the corresponding phase header passes on a fresh run.

### Parallel opportunities

- T002 + T003 (independent files)
- T004 + T005 + T006 + T007 + T008 (independent domain files, all `[P]`)
- T009 + T010 (independent test files, same story)
- T017 + T018 (independent test files, same story)
- T027 + T030 + T031 + T033 + T034 (different concerns, independent files)
- Whole user-story phases (US1, US2, US3, US4) can run in parallel once Foundational completes.

---

## MVP scope

User Story 1 + Foundational + Setup is the smallest deliverable that brings real value: registrations stop accepting clearly ineligible beneficiaries while preserving the region-99 bypass under audit. Ship US1 first, then layer US2/US3/US4 in parallel based on team capacity.

---

## Acceptance mapping (FR → tasks)

| FR | Covered by |
|---|---|
| FR-001 | T011 |
| FR-002 (type A, `INCOME_AND_NO_DEPENDENTS` only — clarification CL-Q1) | T009, T011 |
| FR-003, FR-004 | T009, T011 |
| FR-005 (region-99 short-circuit) | T009, T010, T011 |
| FR-006 (RegionBypassEvaluated WARN) | T012, T016 |
| FR-007 (boot guard) | T028, T029 |
| FR-008 (region-99 report) | T019, T020, T021, T017, T018 |
| FR-009 (simulate endpoint) | T024, T025, T023 |
| FR-010 (invalid input → structured) | T009, T011 |
| FR-011 (documented reasons) | T004 |
| FR-012 (port surface minimal) | T007, T027 |
| NFR-PERF-001 | T031 |
| NFR-SEC-001 | T023 |
| NFR-SEC-002 | T017, T021 |
| NFR-OBS-001 | T030 |
| NFR-OBS-002 | T030 (alert rule committed alongside metric) |
| Constitution I (Legacy Traceability) | T032 |
| Constitution II (Modular Monolith) | T027 |
| Constitution III (Test-First) | T009, T010, T014, T017, T018, T023, T029, T032 |
| Constitution VI (region-99 audit, masking) | T012, T016, T020 |
| Constitution VII (observability) | T030 |
