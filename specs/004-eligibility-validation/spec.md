# Feature Specification: Eligibility Validation

**Feature Branch**: `004-eligibility-validation`
**Created**: 2026-05-20
**Status**: Draft
**Bounded Context**: `SocialProgramRegistry` (eligibility lives next to program rules; see [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §3)
**Input**: User description: *"Replace the legacy `VALELEG.NSN` eligibility validator with a transparent, testable rule engine that enforces program-type rules (A/P/T), preserves the region-99 bypass (BR-024 / MYS-008) under an audited flag, and exposes a `BeneficiaryEligibilityPort` consumed by Beneficiary Registration and Payment Cycle."*

## Why this feature exists

- **Owns BR-024 / MYS-008**: the "bypass do Roberto" — region code 99 lets a beneficiary skip all eligibility checks. The legacy does this silently. The new system must keep the behavior (until SENARC rules otherwise) but make every use auditable.
- **Owns BR-025 to BR-027**: type-A income/dependents thresholds, type-P age ≥ 60, type-T age 16–65.
- **Consumed by 002 and 001**: 002 calls it on registration; 001 calls it during the cycle's snapshot.

## Clarifications

### Session 2026-05-20

- Q: Type-A rejection reason — `INCOME_ABOVE_LIMIT` vs `INCOME_AND_NO_DEPENDENTS`? → A: keep `INCOME_AND_NO_DEPENDENTS` only; BR-025 is a compound rule, the name should reflect both conditions failing.
- Q: Snapshot consistency when a program changes mid-cycle? → A: snapshot at `cycle.startedAt`. The in-flight cycle uses the program data frozen at start; admin updates apply to the next cycle. This matches feature 001's existing beneficiary snapshot discipline and keeps the cycle deterministic. Equivalence with legacy holds across cycles even though within a single cycle the legacy re-read per call — documented divergence justified by audit clarity.
- Q: `/simulate` endpoint policy — rate limit, scope, audit? → A: 30 calls/minute per authenticated user (JWT `sub`), HTTP 429 `application/problem+json` when exceeded. Audit ONLY `EligibleByBypass` outcomes (avoid drowning the audit table with happy-path noise). Per-user (not per-IP) because corporate NAT collapses many operators behind one IP.
- Q: Behavior for region codes outside `{1..26, 99}`? → A: return `Ineligible(INVALID_INPUT, "regionCode=N out of accepted range")`. The cycle records a `cycle_skip` row with reason `INVALID_REGION` and continues processing the remaining rows. Aligns with FR-010's never-throw stance and surfaces legacy bad data in the report without losing the run.

### Open (Q-series, blocking `/speckit.implement`)

_All blocking clarifications resolved as of 2026-05-21._

## User Scenarios & Testing

### User Story 1 — Beneficiary registration calls the validator (Priority: P1) [MVP]

As a registration operator, when I register a beneficiary in program `BFA1` (type A), the system MUST validate that the beneficiary meets type-A criteria before persisting.

**Independent Test**: Register a beneficiary with `programCode=BFA1, familyIncome=550, dependents=2`. Validator returns `Eligible`. Beneficiary is persisted. Register another with `programCode=BFA1, familyIncome=2000, dependents=0`. Validator returns `Ineligible(reason=INCOME_AND_NO_DEPENDENTS)`. Registration fails with 422.

**Acceptance Scenarios**:

1. **Given** a type-A program with `incomeMax=600` (the dependents threshold of `≥ 1` is hard-coded by BR-025, not a program parameter), **When** a beneficiary has `income=550, dependents=2`, **Then** result is `Eligible`.
2. **Given** the same program, **When** `income=900, dependents=0`, **Then** result is `Ineligible(INCOME_AND_NO_DEPENDENTS)`.
3. **Given** the same program, **When** `income=900, dependents=2`, **Then** result is `Eligible` (BR-025: high income OK if ≥ 1 dependent).
4. **Given** any program with `regionCode=99`, **When** validator runs, **Then** result is `EligibleByBypass(regionCode=99)` and a `WARN`-severity audit event is published (BR-024).

### User Story 2 — Payment cycle uses the validator (Priority: P1)

As the PaymentCycle module, during the run I call the validator for each beneficiary using the snapshot data. If the validator returns `Ineligible`, the cycle records a `CycleSkip` row with the reason.

**Acceptance Scenarios**:

1. **Given** an active beneficiary who became ineligible after registration (e.g., income changed), **When** the cycle runs, **Then** the beneficiary is skipped with reason `Ineligible`.
2. **Given** an active beneficiary with `regionCode=99`, **When** the cycle runs, **Then** the beneficiary is processed (`EligibleByBypass`), the `RegionBypassUsed` event is emitted (already in 001 FR-023), and `region99_bypass_count` is incremented.

### User Story 3 — Admin reviews region-99 usage (Priority: P1)

As an SENARC analyst, I want to see how many beneficiaries used region-99 in the last cycle, per program, so we can decide whether to remove the bypass.

**Acceptance Scenarios**:

1. **Given** a completed cycle, **When** analyst queries `GET /api/v1/eligibility/region-99-report?cycleId=42`, **Then** the response lists the beneficiaries (masked CPF) and a summary count.

### User Story 4 — Operator simulates an eligibility check (Priority: P2)

As a registration operator, I can pre-check whether a candidate would be eligible before submitting their full data, so the UI can warn early.

**Independent Test**: `POST /api/v1/eligibility/simulate {programCode, birthDate, familyIncome, dependents, regionCode}` returns `{result, reason}` without persisting anything.

### Edge Cases

- **Program type T with age = 16 exactly** — eligible (boundary inclusive).
- **Program type P with birthDate today − 60y exactly** — eligible (boundary inclusive).
- **`regionCode = 99` combined with type-T age 70** — `EligibleByBypass`; the bypass wins (BR-024 is unconditional).
- **Program not found** — validator returns `Ineligible(reason=PROGRAM_NOT_FOUND)`, no exception.
- **Program `Retired`** — `Ineligible(reason=PROGRAM_RETIRED)`.
- **Family income negative** — `Ineligible(reason=INVALID_INPUT)`. The legacy crashed; we now return a structured result.
- **Unknown region code** (e.g. `regionCode = 50`, `0`, `127`) — `Ineligible(reason=INVALID_INPUT, detail="regionCode=50 out of accepted range")`. Cycle skips with reason `INVALID_REGION` and continues. Surfaces legacy bad data without crashing the run.

## Requirements

### Functional (FR)

| ID | Requirement | Source |
|---|---|---|
| FR-001 | The system MUST expose `validate(programCode, birthDate, familyIncome, dependents, regionCode)` returning one of `Eligible`, `EligibleByBypass`, `Ineligible(reason, detail)`. The validator is stateless; **snapshot consistency is the caller's responsibility**. Feature 001 passes program data resolved at `cycle.startedAt`; feature 002 passes today's data. | `VALELEG.NSN#L80-L210` |
| FR-002 | For program type A: `Eligible` IFF `(income ≤ programIncomeMax)` OR `(dependents ≥ 1)`. Sole reason on failure: `INCOME_AND_NO_DEPENDENTS`. | `VALELEG.NSN#L155-L168`, BR-025 |
| FR-003 | For program type P: `Eligible` IFF `age ≥ 60` at the reference date. Reason on failure: `AGE_BELOW_60`. | `VALELEG.NSN#L170-L175`, BR-026 |
| FR-004 | For program type T: `Eligible` IFF `16 ≤ age ≤ 65`. Reasons: `AGE_BELOW_16` or `AGE_ABOVE_65`. | `VALELEG.NSN#L177-L182`, BR-027 |
| FR-005 | When `regionCode = 99`, the system MUST short-circuit and return `EligibleByBypass(regionCode=99)`, **regardless of program criteria** (BR-024). Input validation (null fields, negative income/dependents) still runs first; an invalid input returns `Ineligible(INVALID_INPUT)` and never reaches the bypass. Evaluation order: input validation → bypass → program lookup → program retired → type-specific rule. | `VALELEG.NSN#L106-L110` |
| FR-006 | Every `EligibleByBypass` result MUST publish a `RegionBypassEvaluated` audit event with severity `WARN`. | `[GREENFIELD]` — hardening for MYS-008; legacy silently bypassed without trail |
| FR-007 | The system MUST refuse to start in production when `sifap.eligibility.region99Bypass.enabled = false` AND there exists at least one beneficiary with `regionCode = 99` in the database. Operator must explicitly migrate those records first. | `[GREENFIELD]` — defense in depth around MYS-008; legacy had no equivalent |
| FR-008 | The system MUST expose `GET /api/v1/eligibility/region-99-report` with parameters `cycleId` or `(competence, programCode)`. Exactly one of the two filter modes MUST be present (400 if neither, 400 if both). The response MUST be paginated when row count exceeds 1000 (cursor + limit query params). | `[GREENFIELD]` — SENARC oversight; legacy lacked this report |
| FR-009 | The system MUST expose `POST /api/v1/eligibility/simulate` for pre-validation (no persistence). Rate limit: **30 calls/min per authenticated user** (keyed by JWT `sub`); on exceed return HTTP 429 with RFC 7807 body. Audit policy: only `EligibleByBypass` outcomes emit a `RegionBypassEvaluated` event (same event used by FR-006); other outcomes are not audited. | `[GREENFIELD]` — registration UX; legacy had no pre-check |
| FR-010 | Negative `familyIncome`, `dependents < 0`, or `regionCode` outside the accepted range `{1..26 mapped to UFs, 99 = bypass}` MUST return `Ineligible(reason=INVALID_INPUT)` without throwing. The cycle records such rows in `cycle_skip` with reason `INVALID_REGION` (when applicable) and continues. | `[GREENFIELD]` — legacy crashed on invalid input |
| FR-011 | Each `Ineligible` reason MUST map to a documented enum value with stable text. | Constitution Principle VII (observability) |
| FR-012 | The published port (consumed by 001 and 002) MUST expose only `validate(...)` — admin/audit endpoints stay in the REST layer. | Module boundary |

### Non-Functional (NFR)

| ID | Requirement |
|---|---|
| NFR-PERF-001 | `validate` MUST return in P95 ≤ 5 ms (single in-process call, no DB round-trip — program data fetched once per cycle and cached). |
| NFR-SEC-001 | Simulate endpoint MUST require any authenticated role (no anonymous access). |
| NFR-SEC-002 | Region-99 report MUST require `ADM` or `AUD` role. |
| NFR-OBS-001 | Counters: `sifap.eligibility.evaluated.count{result}`, `sifap.eligibility.region99.count{program}`. |
| NFR-OBS-002 | Alert: `sifap.eligibility.region99.count > baseline + 3σ` on any program → INFO (anomaly signal). |

### Constraints (CON)

| ID | Constraint |
|---|---|
| CON-001 | The validator is **pure** — no DB writes, no side effects other than audit publish. |
| CON-002 | The region-99 short-circuit is hard-coded and uncovered by feature flag in the workshop scope. Removing it requires SENARC sign-off + a new ADR superseding the relevant section of ADR-008's sibling. |
| CON-003 | Age calculation uses `Period.between(birthDate, referenceDate).getYears()` — matches BR-005 legacy behavior of ignoring month/day until a future Phase 2. |

### Out of Scope (OUT)

- Income reverification against external systems (Receita Federal API).
- Family-tree-based dependent validation.
- Anti-fraud cross-checks across programs.

## Key Entities

| Entity | Description | Key Fields |
|---|---|---|
| `EligibilityResult` (sealed) | Result of a validation. | `Eligible`, `EligibleByBypass(regionCode)`, `Ineligible(reason, detail)` |
| `Reason` (enum) | Documented ineligibility reasons. | `INCOME_AND_NO_DEPENDENTS`, `AGE_BELOW_60`, `AGE_BELOW_16`, `AGE_ABOVE_65`, `PROGRAM_NOT_FOUND`, `PROGRAM_RETIRED`, `INVALID_INPUT` |

## Success Criteria

1. Region-99 audit count matches `payment.region_bypass = true` count after a cycle (cross-feature consistency).
2. ≥ 95% coverage on `EligibilityValidator`; boundary tests for ages 16, 60, 65 and income thresholds.
3. Validator returns within 5 ms median (microbenchmark in CI).
4. Operators can query the region-99 report for any cycle and reconcile the numbers with the payment table.

## References

- Plan: [`plan.md`](plan.md) · Tasks: [`tasks.md`](tasks.md)
- Business rules: BR-024 (region-99), BR-025/026/027 (type A/P/T), BR-005 (age calc)
- Mysteries addressed: MYS-008 (region 99 bypass), partially MYS-001 (status S overload through Ineligible reasons)
- Legacy: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN`
- Consumes: feature 003 via the 004-owned port `ProgramCriteriaPort` (an adapter that maps `SocialProgramQueryPort.ProgramView` to the local `EligibilityCriteria` shape, supplying `incomeMax` which 003's port does not expose). The adapter is needed because 003 deliberately keeps eligibility data out of its published port.
- Consumed by: feature 001 (cycle skip on Ineligible), feature 002 (registration block on Ineligible)
