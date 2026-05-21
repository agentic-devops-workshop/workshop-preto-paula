# Tasks: Social Program Registry

| ID | Task | Pri | Files | DoD | Blocked by |
|---|---|---|---|---|---|
| T001 | Flyway V001 — `social_program`, `regional_factor` tables | P1 | `db/migration/socialprogram/V001*.sql` | Migration green; constraints reject invalid type/range/region-99 | — |
| T002 | Domain enums + value objects — `ProgramType`, `ProgramStatus` (with transitions) | P1 | `domain/*.java` | Unit tests cover `Active→Suspended→Retired` matrix; `Retired` is terminal | — |
| T003 | `SocialProgram` aggregate + `RegionalFactor` entity | P1 | `domain/SocialProgram.java`, `domain/RegionalFactor.java`, events | Behavior methods: `changeAdjustmentFactor`, `replaceRegionalFactors`, `changeStatus`; 27-UF whitelist enforced | T002 |
| T004 | Repositories | P1 | `infrastructure/persistence/*.java` | Testcontainers integration test on unique + FK | T001, T003 |
| T005 | `SocialProgramQueryPort` published in `api/` | P1 | `api/SocialProgramQueryPort.java` | Matches feature 001's expected interface byte-for-byte | T003 |
| T006 | `SocialProgramService` (write side) | P1 | `application/SocialProgramService.java` | Tests: create, duplicate→409, update adjustment, replace factors, status transitions | T004, T005 |
| T007 | Caffeine cache adapter + write-through invalidation | P2 | `infrastructure/cache/CachedSocialProgramAdapter.java` | Read-after-write consistency test; cache hit metric exposed | T006 |
| T008 | REST controllers + DTOs + per-module exception handler | P1 | `interfaces/*.java`, `interfaces/dto/*.java` | RestAssured covers all 7 endpoints + RBAC + RFC 7807 | T006 |
| T009 | ArchUnit suite — only `api/` package importable by other contexts | P1 | `src/test/.../SocialProgramArchitectureTest.java` | Cross-context imports of `domain/`, `application/`, `infrastructure/` fail-build | T008 |
| T010 | Contract test against feature 001 port | P1 | `src/test/.../SocialProgramPortContractTest.java` | Signature comparison passes (uses reflection to compare interface methods) | T005 |
| T011 | Observability — `sifap.program.{cache.hit,cache.miss,updates}` counters | P2 | `MetricsConfig` extension | Metrics visible at `/actuator/prometheus` | T007 |
| T012 | README + glossary entries (`SocialProgram`, `RegionalFactor`, `ProgramStatus`) | P3 | `specs/003-.../README.md`, `01-arqueologia/glossary.md` | Cross-links open | T008 |
