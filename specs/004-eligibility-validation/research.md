# Phase 0 — Research

**Feature**: 004 Eligibility Validation · **Date**: 2026-05-20

## Purpose

Resolve every `NEEDS CLARIFICATION` from the plan's Technical Context, plus the technology and pattern choices that shape the design. Each entry below is a decision the team has converged on, with its rationale and the alternatives rejected.

---

## R-001 — Reference date for age calculation

**Decision**: caller supplies `referenceDate` as a parameter to `validate(...)`. The validator never reads the system clock.

**Rationale**:

- Feature 002 (Registration) passes `LocalDate.now()`.
- Feature 001 (PaymentCycle) passes `cycle.snapshotAt` so the entire cycle is reproducible.
- Property tests need to fix the clock; a parameter is the only clean way.
- Matches Constitution Principle V (Single Source of Truth) — there is exactly one place where time enters: the caller.

**Alternatives considered**:

- *Internal `LocalDate.now()` in the validator*: breaks 001 snapshot consistency.
- *Two methods (`validate(...)` + `validateAt(referenceDate, ...)`)*: doubles the API surface for no real benefit.
- *Request-scoped Spring bean carrying the clock*: hidden coupling, untestable.

---

## R-002 — Snapshot consistency across program updates mid-cycle

**Decision**: open — pending answer to clarification Q2 in `spec.md`. **NEEDS CLARIFICATION**.

**Working assumption for Phase 1 design**: feature 003's `SocialProgramQueryPort` adapter is responsible for snapshot consistency (the cycle pre-loads the program view once at `cycle.startedAt` and reuses it for every row). The eligibility validator is stateless and uses whatever criteria the caller provides. This puts the burden of consistency at the right layer.

**Alternatives considered** (will be reconciled when Q2 is answered):

- *Eligibility caches criteria internally*: violates the "pure function" constraint and duplicates 003's job.
- *Re-validate at each row read from DB*: would let a mid-cycle program update break equivalence (FR-EQUIV-001 in feature 001).

---

## R-003 — Sealed result type vs exceptions

**Decision**: sealed interface `EligibilityResult` with three variants `Eligible`, `EligibleByBypass(short)`, `Ineligible(reason, detail)`.

**Rationale**:

- Java 21 sealed types give exhaustive pattern matching at the call site, removing the "did I handle every case?" bug.
- Aligns with FR-010 (negative input returns structured `Ineligible`, not an exception).
- Aligns with constitution Principle VII (observability) — the result type itself is the diagnostic.

**Alternatives considered**:

- *Boolean + side-channel reason*: invites callers to ignore the reason.
- *Throw on Ineligible*: makes the happy path expensive (exception construction in a hot path of ~4 M calls/cycle) and conflates control flow with errors.
- *Functional `Either<Reason, Eligible>`*: works but adds Vavr dependency, banned by Constitution Principle IV.

---

## R-004 — Region-99 short-circuit position in the evaluation order

**Decision**: place the region-99 check **after** input validation but **before** program lookup, exactly as in `EligibilityValidator.validate(...)` (the implementation already in `domain/EligibilityValidator.java`).

**Rationale**:

- BR-024 says the bypass overrides everything *valid*. It should not paper over invalid input (null birth date, negative income). This matches the legacy behavior captured during archaeology.
- Putting it before program lookup means we never need to hit feature 003 for a region-99 beneficiary — saves the DB call in the worst case (0% impact on perf budget, but the right semantics).
- Audit event `RegionBypassEvaluated` is published from the **adapter** (`DefaultEligibilityPortAdapter`), not the validator, so the validator stays pure.

**Alternatives considered**:

- *Bypass as the very first check, before input validation*: would silently accept `null` birth dates. Bad.
- *Bypass at the end, after type-specific rules*: changes equivalence vs the legacy (which short-circuits early).

---

## R-005 — Audit event severity and channel

**Decision**: `WARN` severity, published as a Spring Application Event consumed by `ReportingAndAudit`. No transport (Service Bus) needed inside the monolith — ADR-001 already justifies this.

**Rationale**:

- WARN is the right level: not an error (it's by design), but not noise either (auditors must see it).
- Application events stay in-process; the audit subscriber writes the row inside the same transaction as the consuming feature (001 cycle, 002 registration), giving us atomicity for free.
- Decouples the validator from the audit infrastructure, satisfying Constitution Principle V.

**Alternatives considered**:

- *INFO severity*: gets filtered out of operator dashboards. Defeats the purpose.
- *ERROR severity*: would page on-call for normal behavior. Cry-wolf risk.
- *Direct repository write from the adapter*: cross-module direct write — violates Constitution Principle II.

---

## R-006 — Property-based tests with jqwik

**Decision**: add jqwik to the test scope and use it for the validator (T003). The runtime stack stays untouched.

**Rationale**:

- The validator has a small input space but high branching density (type A/P/T × region 99 × age boundaries × income brackets). Property tests catch combinations a hand-written matrix misses.
- jqwik is a test-only dependency, compatible with JUnit 5, and already used elsewhere in the workshop.
- The invariant we want to assert ("bypass dominates") is a single line in jqwik.

**Alternatives considered**:

- *Hand-written parameterized tests only*: easy to miss the boundary at age 16 + income 600.01.
- *QuickTheories*: less mature integration with JUnit 5.

---

## R-007 — Boot-time guard implementation

**Decision**: `@Profile("prod")` `@Configuration` with a `@PostConstruct` that runs a `SELECT COUNT(*)` against `beneficiary` filtered by `region_code = 99` when the bypass flag is `false`. If the count is > 0, throw `IllegalStateException` and let Spring fail the boot.

**Rationale**:

- Same pattern as `CpfBackdoorBootGuard` (feature 002) and `CycleSchedulerBootGuard` (feature 001) — consistency reduces ops cognitive load.
- One SQL query at startup is cheap.
- Failing the boot is the only way to make the dependency on legacy data explicit.

**Alternatives considered**:

- *Runtime check at the first validate call*: too late, would mean a half-up boot with broken semantics.
- *Build-time check via a Maven plugin*: doesn't know the production DB state.

---

## R-008 — Region-99 report data source

**Decision**: read from `payment.region_bypass = true` (feature 001's persisted column) when querying by `cycleId`; read from beneficiary rows when querying by `(competence, programCode)` for a future cycle that hasn't run.

**Rationale**:

- Avoids duplicating bypass tracking. The bypass flag is already persisted exactly where it needs to be.
- Cross-cycle reconciliation (Success Criterion #1) is trivial when the count comes from `payment` directly.

**Alternatives considered**:

- *Replay audit events*: requires event-store style query for a metric that's a single SELECT.
- *Maintain a denormalized bypass count on `payment_cycle`*: extra write path, drift risk.

---

## Open items rolled up to spec

| Code | Topic | Status |
|---|---|---|
| Q1 | Type-A reason naming | ✅ Resolved 2026-05-20 → `INCOME_AND_NO_DEPENDENTS` only |
| Q2 | Mid-cycle program update consistency | ⏳ Open in `/speckit.clarify` queue |
| Q3 | Region-99 report storage source | ✅ Resolved here as R-008 |
| Q4 | Simulate-endpoint audit & rate limit | ⏳ Open in `/speckit.clarify` queue |
| Q5 | Reference date semantics | ✅ Resolved here as R-001 |
