# Tasks: Eligibility Validation

| ID | Task | Pri | Files | DoD | Blocked by |
|---|---|---|---|---|---|
| T001 | Domain — `Reason` enum, `EligibilityCriteria` record, `EligibilityResult` sealed interface | P1 | `domain/*.java` | Compiles; exhaustive pattern-matching test asserts the three variants are reachable | — |
| T002 | `EligibilityValidator` pure function with region-99 short-circuit | P1 | `domain/EligibilityValidator.java` | 100% line coverage; boundary tests for ages 16/18/60/65 and income thresholds | T001 |
| T003 | Property test (jqwik) for the validator | P1 | `src/test/.../EligibilityValidatorPropertyTest.java` | Random inputs satisfy invariant: bypass dominates; otherwise reason matches rule | T002 |
| T004 | Published port `api/BeneficiaryEligibilityPort` + adapter | P1 | `api/BeneficiaryEligibilityPort.java`, `infrastructure/DefaultEligibilityPortAdapter.java` | Contract test ensures signature matches the version expected by 001 and 002 | T002 |
| T005 | `ProgramCriteriaPort` read-only port consumed from SocialProgramRegistry | P1 | `application/ports/ProgramCriteriaPort.java` | Interface only; impl lives in 003 once features merge | — |
| T006 | `EligibilityService` — `simulate` + region-99 report aggregation | P1 | `application/EligibilityService.java` | Tests: simulate path uses no DB; report aggregates correctly with mocked data | T004, T005 |
| T007 | REST controller — `/simulate` + `/region-99-report` + DTOs | P1 | `interfaces/*.java`, `interfaces/dto/*.java` | RestAssured: 200/400/401/403 + RFC 7807 errors | T006 |
| T008 | `Region99StartupGuard` (`@Profile("prod")`) | P1 | `infrastructure/config/Region99StartupGuard.java` | Integration test asserts boot fails when bypass disabled AND region-99 rows present | T004 |
| T009 | `SpringEventAuditPublisher` emitting `RegionBypassEvaluated` at WARN | P1 | `infrastructure/SpringEventAuditPublisher.java` | Spring test asserts one event per bypass result | T002 |
| T010 | Observability — `sifap.eligibility.*` counters + bypass anomaly alert rule | P2 | `MetricsConfig`, alert YAML | `/actuator/prometheus` shows counters; alert rule committed | T006 |
| T011 | ArchUnit — only `api/` may be imported by other modules; `EligibilityValidator` must be in `domain/` | P1 | `src/test/.../EligibilityArchitectureTest.java` | Cross-context imports of non-api packages fail-build | T007 |
| T012 | Microbenchmark — 100k validations ≤ 1s | P2 | `src/test/.../perf/EligibilityValidatorBench.java` | Test green; report archived in CI artifacts | T002 |
| T013 | README + glossary updates (`EligibilityResult`, `Reason`, `Region 99`) | P3 | `specs/004-.../README.md`, `01-arqueologia/glossary.md` | Cross-links open; region-99 entry references MYS-008 | T007 |
