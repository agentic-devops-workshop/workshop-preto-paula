# Feature Specification: Monthly Payment Cycle Generation

**Feature Branch**: `001-payment-cycle-generation`
**Created**: 2026-05-20
**Status**: Draft
**Bounded Context**: `PaymentProcessing` (see [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §4)
**Input**: User description: *"Replace the legacy `BATCHPGT.NSN` monthly batch with an idempotent, observable, equivalence-tested payment cycle generator that preserves every business rule from 29 years of production behavior, including the December 13th-payment, the type-A Christmas allowance, and mainframe truncation semantics."*

## User Scenarios & Testing

### User Story 1 — Operator triggers monthly cycle (Priority: P1) [MVP]

As a payment operations analyst, I trigger the monthly payment cycle for a given competence (YYYY-MM) and program, and the system generates exactly one payment record per active beneficiary in that program, with values computed using the legacy formula and persisted atomically.

**Why this priority**: This is the single most critical batch in SIFAP. 4.2M beneficiaries × 12 months × 29 years = 180M payment rows over the system's lifetime. Without this, no money reaches anyone. P1, MVP.

**Independent Test**: Trigger a cycle for competence `2026-06` with a fixture of 100 beneficiaries (80 Active, 10 Suspended, 10 Cancelled). Result: 80 `payment` rows, all with status `Pending`, sorted by CPF, with `cycleId` populated. Re-triggering the same competence produces zero new rows (idempotency).

**Acceptance Scenarios**:

1. **Given** 80 active beneficiaries in program `BFA1`, **When** operator triggers cycle for `2026-06` on `BFA1`, **Then** 80 payment rows are created, each linked to the `payment_cycle` row, with `status = Pending` and `competence = 2026-06`.
2. **Given** the cycle was already executed for `2026-06`/`BFA1`, **When** operator triggers it again, **Then** the system returns `409 Conflict` with `code = CYCLE_ALREADY_GENERATED` and no new rows are created (BR-034).
3. **Given** a beneficiary in `Suspended` lifecycle, **When** the cycle runs, **Then** no payment row is created for that beneficiary and an audit event `BeneficiarySkipped(reason=Suspended)` is published.
4. **Given** the program is `Inactive`, **When** operator triggers the cycle, **Then** the system returns `409 Conflict` with `code = PROGRAM_INACTIVE` and no rows are created (BR-035).
5. **Given** the cycle runs successfully, **When** querying `GET /api/v1/payment-cycles/{id}`, **Then** the response shows `status = Completed`, `generatedCount`, `skippedCount`, `startedAt`, `completedAt` and a list of skip reasons.

### User Story 2 — December cycle applies 13th-payment rules (Priority: P1) [MVP]

As a payment operations analyst, when I generate the December cycle, the system automatically produces the 13th payment with the reduced formula (no family factor, no income factor) and, for type-A programs, adds the 15% Christmas allowance line.

**Why this priority**: December 2026 is the first production cycle SIFAP 2.0 must serve. Missing the 13th or the type-A bonus is a front-page-news incident. P1, MVP.

**Independent Test**: Run cycle for `2026-12` with 10 beneficiaries (5 in program `BFA1` type `A`, 5 in program `BFP1` type `P`). Result: 10 `payment` rows with `paymentType = THIRTEENTH`, the 5 type-A rows have `christmasAllowance > 0` equal to 15% of base, type-P rows have `christmasAllowance = 0`.

**Acceptance Scenarios**:

1. **Given** competence `2026-12` and a type-A program, **When** the cycle runs, **Then** each generated row has `paymentType = THIRTEENTH`, `grossAmount` computed with `BASE × FREG × FIDADE × (1+REAJ)` only (no family or income factor — BR-018), and `christmasAllowance = round_down(BASE × 0.15)` (BR-019).
2. **Given** competence `2026-12` and a type-P program, **When** the cycle runs, **Then** `paymentType = THIRTEENTH` is set but `christmasAllowance = 0` (BR-019 restricts the bonus to type A).
3. **Given** competence `2026-07` (any non-December month), **When** the cycle runs, **Then** `paymentType = MONTHLY` and `christmasAllowance = 0`.

### User Story 3 — Equivalence with legacy preserved (Priority: P1)

As a quality engineer, the new cycle generator produces byte-for-byte the same payment values as the legacy `BATCHPGT.NSN` for the same input fixture, so we can deploy without compromising 29 years of audit trail.

**Why this priority**: Without equivalence, the cutover is unsafe and rollback requires reprocessing months of data. P1.

**Independent Test**: A fixture of 1,000 beneficiaries (varied programs, ages, dependents, regions, income brackets) is processed by (a) the captured legacy output and (b) the new Java implementation. **The two output sets must match on every numeric field for every row.**

**Acceptance Scenarios**:

1. **Given** the legacy fixture (`legacy-fixture-2026-05.csv`, captured from the SIFAP UAT environment), **When** the new generator runs against the same input, **Then** the SHA-256 of the sorted output CSV matches the SHA-256 of the legacy output.
2. **Given** the fixture contains a region-99 beneficiary (BR-024 backdoor), **When** the cycle runs, **Then** the row is included with full payment value (the "bypass do Roberto" exception is preserved exactly as in legacy until SENARC decides otherwise).
3. **Given** the fixture contains a beneficiary with a `LEGACY_BACKDOOR` CPF status, **When** the cycle runs, **Then** the payment is generated normally (these are migrated rows with a known-invalid CPF, not a reason to skip).

### User Story 4 — Operator monitors progress (Priority: P2)

As a payment operations analyst, I can monitor a long-running cycle in real time and see how many beneficiaries were processed, how many remain, ETA, and any failures, so I can intervene before downstream SLAs are breached.

**Why this priority**: At 4.2M rows per cycle, the batch takes hours. Observability is required to catch silent failures. P2 (the cycle still works without it, but operations cannot manage it).

**Acceptance Scenarios**:

1. **Given** a running cycle, **When** operator opens the dashboard, **Then** they see `processedCount / totalCount`, current rate (rows/s), ETA, and any rows in `Error` state.
2. **Given** the cycle is running, **When** an individual beneficiary fails (e.g., divide-by-zero from corrupted region factor), **Then** the cycle continues for the remaining beneficiaries and the failure is captured in a `cycle_failure` row with stack trace.
3. **Given** the cycle completes, **When** operator clicks "Download report", **Then** they get a CSV with one row per generated payment and a separate CSV with one row per skip/failure.

### User Story 5 — Operator cancels or pauses a cycle (Priority: P3)

As a payment operations analyst, in the rare case I need to abort a cycle mid-flight (e.g., wrong competence picked), I can cancel it and any partial rows are marked `Voided` with a reason.

**Why this priority**: Edge case but legally important — partially generated payments that should not be released. P3.

**Acceptance Scenarios**:

1. **Given** a `Running` cycle, **When** operator submits `POST /api/v1/payment-cycles/{id}/cancel` with a reason, **Then** the cycle stops processing new rows, already-generated rows are marked `status = Voided`, and the cycle moves to `Aborted`.
2. **Given** an `Aborted` cycle, **When** operator tries to cancel again, **Then** the system returns `409 Conflict` with `code = CYCLE_ALREADY_ABORTED`.

### Edge Cases

- **Beneficiary becomes Suspended mid-batch**: read-snapshot consistency — the cycle uses the beneficiary state at `cycle.startedAt`; subsequent lifecycle changes do not affect the in-flight cycle.
- **Cycle for future competence**: `competence > today + 1 month` is rejected with `400 Bad Request` (FR-034).
- **Cycle for past competence > 12 months old**: requires `ADM` role + explicit `forceBackfill = true` flag (NFR-AUDIT-002).
- **All beneficiaries skipped (e.g., all Suspended)**: cycle still completes with `Completed` status and `generatedCount = 0`. Operator gets a warning email.
- **System crash mid-cycle**: cycle is `Running` with `startedAt` more than `idle_timeout` (default 4h) → next operator action sees the cycle as `Stale`; resuming is a separate operation that completes the remaining rows using the same snapshot timestamp.
- **Region factor table missing for one UF**: that beneficiary lands in `Error` with `code = MISSING_REGION_FACTOR`. The cycle does not abort.
- **Rounding edge** (value ends in `0.0049...`): truncation per BR-020 (always down). Test fixture covers this explicitly.

## Requirements

### Functional Requirements (FR)

| ID | Requirement | Source |
|---|---|---|
| FR-001 | Operator MUST be able to trigger a payment cycle by `(competence, programCode)`. | `BATCHPGT.NSN#L120-L168` |
| FR-002 | The system MUST refuse generation if a Completed or Running cycle already exists for the same `(competence, programCode)` (BR-034). | `BATCHPGT.NSN#L196-L208` |
| FR-003 | The system MUST refuse generation if the program is `Inactive` (BR-035). | `BATCHPGT.NSN#L219-L223` |
| FR-004 | The system MUST process beneficiaries sorted ascending by CPF (BR-033). | `BATCHPGT.NSN#L175-L179` |
| FR-005 | The system MUST skip beneficiaries whose `lifecycleStatus` is not `Active` at cycle start, logging skip reason. | `BATCHPGT.NSN#L180-L195` |
| FR-006 | The system MUST compute `grossAmount = BASE × FREG × FFAM × FRND × FIDADE × (1 + REAJ)` for monthly payments (BR-017). | `CALCBENF.NSN#L224-L231` |
| FR-007 | The system MUST compute the regional factor (`FREG`) from the program's 27-UF table (BR-012). | `CALCBENF.NSN#L88-L114` |
| FR-008 | The system MUST compute the family factor (`FFAM`) using the progressive scale: 0 deps = 1.00, 1-2 = +5% each, 3-4 = 1.10 + 3% each, 5+ = 1.16 + 2% each (BR-014). | `CALCBENF.NSN#L159-L172` |
| FR-009 | The system MUST compute the income factor (`FRND`) using the inverse table: ≤300 = 100%, …, >1500 = 40% (BR-015). | `CALCBENF.NSN#L116-L128` |
| FR-010 | The system MUST compute the age factor (`FIDADE`): ≥65 = +15%, ≥60 = +10%, <18 = +5%, otherwise 0% (BR-016). | `CALCBENF.NSN#L205-L218` |
| FR-011 | The system MUST compute `kFactor = 1.00 + (FATOR-REAJUSTE × 0.347215)` as the `(1+REAJ)` term (BR-010, MYS-003). | `CADPROG.NSN#L81-L82` |
| FR-012 | The system MUST round monetary values DOWN (truncate, never half-up) to 2 decimals (BR-020). | `CALCBENF.NSN#L233-L235` |
| FR-013 | For competence in December (`MM = 12`), the system MUST set `paymentType = THIRTEENTH` and compute `grossAmount = BASE × FREG × FIDADE × (1 + REAJ)` (omitting FFAM and FRND, BR-018). | `CALCBENF.NSN#L239-L245` |
| FR-014 | For December cycles in a type-A program, the system MUST add `christmasAllowance = truncate(BASE × 0.15, 2)` (BR-019). | `CALCBENF.NSN#L248-L254` |
| FR-015 | For non-December cycles, `paymentType = MONTHLY` and `christmasAllowance = 0`. | `BATCHPGT.NSN#L143-L150` |
| FR-016 | The system MUST persist the generated rows in a single transaction per batch chunk (default 1000 rows) with retry on transient DB error. | `[GREENFIELD]` — Spring Batch chunk-oriented step pattern |
| FR-017 | The system MUST publish a `PaymentGenerated` event per row for the `ReportingAndAudit` context. | `[GREENFIELD]` — replaces legacy AUDITORIA writes |
| FR-018 | The system MUST persist a `payment_cycle` aggregate row with totals (`generatedCount`, `skippedCount`, `errorCount`, `totalGrossAmount`). | `BATCHPGT.NSN#L261-L289` |
| FR-019 | The system MUST emit operator-visible progress every 10s during the run (NFR-OBS-004). | `[GREENFIELD]` — legacy had no progress signal |
| FR-020 | The system MUST allow `POST /api/v1/payment-cycles/{id}/cancel` to abort a running cycle; partially generated rows MUST be flipped to `status = Voided`. | `[GREENFIELD]` — legacy required DBA SQL to cancel |
| FR-021 | The system MUST refuse a future-dated competence (`> currentMonth + 1`) with `400 Bad Request`. | `[GREENFIELD]` — legacy accepted any value |
| FR-022 | The system MUST require `ADM` role + explicit `forceBackfill = true` flag for competences older than 12 months. | `[GREENFIELD]` — risk control |
| FR-023 | Region code `99` MUST bypass the eligibility checks **but still receive the payment** (BR-024, MYS-008). Each such row MUST emit a `RegionBypassUsed` audit event with `WARN` severity. | `VALELEG.NSN#L106-L110` |
| FR-024 | The system MUST process beneficiaries with `cpfValidationStatus = LEGACY_BACKDOOR` normally (these are migrated rows). | `[GREENFIELD]` — see ADR-008 |
| FR-025 | When a row computation fails, the cycle MUST capture the failure in `cycle_failure(cycleId, beneficiaryId, reason, stackTrace)` and continue. | `[GREENFIELD]` — legacy aborted the whole batch on first error |
| FR-026 | The system MUST expose `GET /api/v1/payment-cycles/{id}` with full status; and `GET /api/v1/payment-cycles?competence=&program=&status=` with cursor pagination. | `[GREENFIELD]` |
| FR-027 | The system MUST expose `GET /api/v1/payment-cycles/{id}/report.csv` with a CSV of all generated rows (NFR-COMP-002 retention). | `BATCHREL.NSN#L80-L140` |
| FR-028 | The system MUST log every cycle trigger with `correlationId`, `userId`, `competence`, `programCode` in JSON-structured logs (NFR-OBS-001). | `[GREENFIELD]` |
| FR-029 | The system MUST NOT modify any beneficiary, dependent, or program data — write-only to `payment`, `payment_cycle`, `cycle_failure`. | `[GREENFIELD]` — module boundary, ADR-001 |
| FR-030 | The system MUST be schedulable via Quartz with a default trigger `0 0 2 1 * ?` (1st of every month at 02:00 America/Sao_Paulo). The trigger is **disabled by default in production** until SENARC sign-off. | `[GREENFIELD]` — legacy was started manually by ops |

### Functional Equivalence (FR-EQUIV)

| ID | Requirement | Source |
|---|---|---|
| FR-EQUIV-001 | For the fixture `legacy-fixture-2026-05.csv` (~1,000 rows), the SHA-256 of the new implementation's output MUST equal the SHA-256 of the legacy output. | Equivalence harness |
| FR-EQUIV-002 | The fixture MUST include at least: 1 region-99 beneficiary, 1 LEGACY_BACKDOOR CPF, 5 December rows for type-A program, 5 December rows for type-P program, 10 rows where mainframe truncation differs from half-up rounding, 1 beneficiary with 5 dependents (BR-006). | Fixture spec |
| FR-EQUIV-003 | A diff tool MUST be available (`mvn test -Dtest=PaymentCycleEquivalenceTest`) that reports row-by-row deltas if SHA mismatch. | Test tooling |

### Non-Functional Requirements (NFR)

| ID | Requirement | Source |
|---|---|---|
| NFR-PERF-001 | Generation rate ≥ 1,000 rows/s sustained on a 4 vCPU / 8GB node (target: 4.2M rows in ≤ 70 minutes). | NFR-PERF baseline |
| NFR-PERF-002 | P95 API latency for cycle status reads ≤ 300ms. | NFR-PERF baseline |
| NFR-AVAIL-001 | A crashed cycle MUST be resumable from the last committed chunk without producing duplicates. | Spring Batch restart support |
| NFR-OBS-001 | All operations MUST emit JSON-structured logs with `correlationId`, `cycleId`, `competence`. | Constitution Principle VII |
| NFR-OBS-002 | Metrics MUST include `sifap.cycle.duration`, `sifap.cycle.generated.count`, `sifap.cycle.error.count`, `sifap.cycle.region99_bypass.count`. | Constitution Principle VII |
| NFR-SEC-001 | Cycle triggering MUST require `OPR` or `ADM` role; cancellation requires `ADM`. | NFR-SEC baseline |
| NFR-SEC-002 | `forceBackfill = true` MUST require `ADM` and emit an audit event with `severity = HIGH`. | Defense in depth |
| NFR-COMP-001 | Every cycle execution MUST produce an immutable audit record retained for 10 years (LGPD/IN-TCU). | Constitution Principle VI |
| NFR-COMP-002 | The `cycle_report.csv` MUST be retained on Azure Blob (hot 90d → cool 1y → archive 10y). | Lifecycle policy |
| NFR-OPS-001 | A SENARC-only feature flag (`sifap.cycle.scheduler.enabled = false`) MUST gate auto-scheduling in production. | Risk control |

### Constraints (CON)

| ID | Constraint | Source |
|---|---|---|
| CON-001 | The cycle MUST run on the modular monolith — no separate microservice (ADR-001). | Constitution Principle II |
| CON-002 | The formula must use `BigDecimal` with explicit `RoundingMode.DOWN` (no `double`/`float` in monetary code). | Stack discipline |
| CON-003 | The cycle does not initiate any external transfer (banking integration is a separate context, `PaymentDisbursement`, not yet implemented). | Module boundary |
| CON-004 | The cycle MUST NOT modify region-99 logic until SENARC issues a written ruling (preserve legacy behavior exactly — MYS-008). | Risk control |

### Out of Scope (OUT)

- **Bank file generation (CNAB 240)**: a separate feature in a future spec.
- **Reconciliation with bank return**: `BATCHCON.NSN` replacement is a separate spec.
- **Tax reporting**: handled by `ReportingAndAudit` context.
- **Re-issuing voided payments**: a separate workflow.

## Clarifications Resolved (2026-05-20)

### CL-001 — Auto-scheduling default state in production

- **Decision**: scheduler **OFF** by default in production (`sifap.cycle.scheduler.enabled = false`). Operator-triggered runs are always available.
- **Rationale**: legacy required manual start; the new system inherits that posture until SENARC formally authorizes auto-run.

### CL-002 — Region-99 bypass

- **Decision**: preserve exactly. Emit `RegionBypassUsed` audit event at `WARN` per occurrence and a daily summary metric.
- **Rationale**: BR-024 / MYS-008 — this is the "bypass do Roberto" from 2012. Removing it without SENARC sign-off creates a production regression for an unknown population of beneficiaries.

### CL-003 — Idle timeout for `Stale` detection

- **Decision**: 4 hours. Configurable via `sifap.cycle.idleTimeout`.
- **Rationale**: a healthy 4.2M-row run completes within ~70 min; a 4h idle window gives operators ample time before a stuck cycle is flagged.

### CL-004 — Backfill cutoff (FR-022)

- **Decision**: 12 months. Older competences require `ADM` + `forceBackfill = true` + a free-text justification stored in audit.
- **Rationale**: aligns with current IN-TCU adjustment window for historical payments.

## Key Entities

| Entity | Description | Key Fields |
|---|---|---|
| `PaymentCycle` | Aggregate root representing one execution. | `id`, `competence`, `programCode`, `status` (Pending/Running/Completed/Aborted/Stale), `startedAt`, `completedAt`, `generatedCount`, `skippedCount`, `errorCount`, `totalGrossAmount`, `triggeredBy`, `correlationId` |
| `Payment` | Single payment row produced by a cycle. | `id`, `cycleId`, `beneficiaryId`, `cpf`, `competence`, `programCode`, `paymentType` (MONTHLY/THIRTEENTH), `grossAmount`, `christmasAllowance`, `status` (Pending/Released/Paid/Voided), `factors` (jsonb: FREG, FFAM, FRND, FIDADE, kFactor) |
| `CycleFailure` | One per beneficiary that errored during the cycle. | `id`, `cycleId`, `beneficiaryId`, `reason`, `stackTrace`, `occurredAt` |
| `CycleSkip` | One per beneficiary skipped due to lifecycle state. | `id`, `cycleId`, `beneficiaryId`, `reason` (NotActive/Ineligible/MissingRegionFactor/ProgramMismatch), `occurredAt` |

## Success Criteria (Definition of Done)

1. **Equivalence proof**: `mvn test -Dtest=PaymentCycleEquivalenceTest` is green on the legacy fixture.
2. **Idempotency proof**: triggering the same `(competence, programCode)` twice produces the same SHA-256 output and zero duplicate rows.
3. **Performance proof**: a load test of 100,000 rows on the dev cluster completes in ≤ 100s (extrapolated from the 1,000 rows/s NFR).
4. **Coverage**: ≥ 90% line coverage on `..paymentprocessing.application..` and 100% on the calculation domain classes.
5. **Constitution compliance**: every FR has `source_legacy`; ArchUnit rules pass; CPF masked in all logs.
6. **Operator demo**: the workshop demo includes triggering a 100-row cycle and showing the resulting `payment` table sorted by CPF.
7. **Audit trail**: every cycle leaves an immutable record in `audit_event` retained for 10 years.
8. **Region-99 preservation**: the test suite includes an explicit assertion that region-99 beneficiaries still receive payment and emit a `RegionBypassUsed` event.

## References

- Plan: [`plan.md`](plan.md)
- Tasks: [`tasks.md`](tasks.md)
- Bounded context: [`../../02-spec-moderna/baseline/bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §4
- Business rules: BR-010, BR-012, BR-014-020, BR-024, BR-033-035 in [`../../01-arqueologia/business-rules-catalog.md`](../../01-arqueologia/business-rules-catalog.md)
- Mysteries addressed: MYS-003 (kFactor magic number), MYS-004 (December rules), MYS-005 (truncation), MYS-008 (region-99)
- ADRs: [001](../../02-spec-moderna/baseline/ADR-001-modular-monolith.md), [005](../../02-spec-moderna/baseline/ADR-005-batch-orchestration.md), [006](../../02-spec-moderna/baseline/ADR-006-data-migration-strategy.md)
- Legacy source: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`, `CALCBENF.NSN`
