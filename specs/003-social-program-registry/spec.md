# Feature Specification: Social Program Registry

**Feature Branch**: `003-social-program-registry`
**Created**: 2026-05-20
**Status**: Draft
**Bounded Context**: `SocialProgramRegistry` (see [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §3)
**Input**: User description: *"Replace the legacy `CADPROG.NSN` registry with a versioned, audited catalog of social programs that owns the eligibility criteria, the base amount, the regional factor table (BR-012) and the adjustment factor that feeds the K-factor (BR-010, MYS-003). Provides a published read-only port consumed by the Payment Cycle module."*

## Why this feature exists

- **Direct dependency of 001**: `PaymentCycleService` requires `SocialProgramQueryPort.findActiveByCode(...)` and `regionalFactor(programCode, regionCode)`. Without 003 in place, the cycle cannot start.
- **Owns MYS-003**: the magic 0.347215 constant in `KFactor` is consumed via `program.adjustmentFactor()`. Centralizing it here means it lives in one place, with one audit trail.
- **Owns BR-012**: the 27-UF regional factor table is per-program, not global. This module is its single source of truth.

## User Scenarios & Testing

### User Story 1 — Operator creates a program (Priority: P1) [MVP]

As a program management analyst with the `ADM` role, I create a new social program with a code, type, base amount and effective dates, so that beneficiaries can be enrolled and a payment cycle can run for it.

**Independent Test**: `POST /api/v1/social-programs` with `code=BFA1, type=A, base=400.00, effectiveFrom=2026-06-01`. Result: `201 Created`, the program appears in `GET /api/v1/social-programs/BFA1`, status `Active`.

**Acceptance Scenarios**:

1. **Given** no program with code `BFA1`, **When** operator submits `POST` with valid body, **Then** the program is persisted with `status = Active`, `version = 1`, an audit event `SocialProgramCreated` is published.
2. **Given** a program with code `BFA1` already exists, **When** operator submits another `POST` with the same code, **Then** the system returns `409 Conflict` `code = PROGRAM_ALREADY_EXISTS`.
3. **Given** an invalid `type` (not in `{A, P, T}`), **When** operator submits, **Then** the system returns `400 Bad Request` `code = VALIDATION_FAILED`.

### User Story 2 — Operator updates the adjustment factor (Priority: P1)

As an admin, I update the adjustment factor of an active program (e.g., quarterly inflation adjustment), so that future payment cycles compute the K-factor (`1 + adj × 0.347215`) using the latest value.

**Independent Test**: `PATCH /api/v1/social-programs/BFA1 {"adjustmentFactor": 0.07}`. Subsequent `GET` returns 0.07. A payment cycle triggered after the update uses 0.07 in `KFactor`. The previous value is preserved in the audit row.

**Acceptance Scenarios**:

1. **Given** an active program `BFA1` with `adjustmentFactor=0.05`, **When** admin sets it to `0.07`, **Then** the new value is persisted, `version` is incremented, and an audit event `ProgramAdjustmentFactorChanged(old=0.05, new=0.07)` is emitted.
2. **Given** an in-flight payment cycle, **When** the adjustment factor changes, **Then** the cycle continues with the snapshot value at `cycle.startedAt` (snapshot consistency, see 001 spec).

### User Story 3 — Operator manages the regional factor table (Priority: P1)

As an admin, I edit the per-UF regional factor table that BR-012 requires, so that the 27 Brazilian states each carry the correct multiplier.

**Independent Test**: `PUT /api/v1/social-programs/BFA1/regional-factors {"SP": 1.20, "RJ": 1.15, …}`. Subsequent `GET /regional-factors/SP` returns `1.20`.

**Acceptance Scenarios**:

1. **Given** an active program, **When** admin submits a complete UF map (exactly 27 entries), **Then** the table is replaced atomically and audited.
2. **Given** a partial UF map, **When** admin submits, **Then** the system returns `400 Bad Request` listing the missing UFs.
3. **Given** any UF map containing a factor `< 0.5` or `> 2.0`, **When** admin submits, **Then** the system returns `400 Bad Request` (sanity range).

### User Story 4 — Payment cycle reads program data (Priority: P1, consumed by 001)

As the PaymentCycle module, I call the published port `SocialProgramQueryPort` and receive the program's type, base, adjustment factor, and the resolved regional factor for a given UF, all from the same consistent snapshot.

**Independent Test**: PaymentCycle integration test wires `SocialProgramQueryPort` to the real adapter and runs a 10-row cycle. The cycle completes; each row's `factors.regional` equals the value stored in the corresponding UF entry.

### User Story 5 — Operator suspends or retires a program (Priority: P2)

As an admin, when a program is being phased out, I suspend new enrollments and eventually retire it, so that new beneficiaries cannot be assigned but existing ones continue to be paid.

**Acceptance Scenarios**:

1. **Given** an `Active` program, **When** admin transitions to `Suspended`, **Then** new beneficiary enrollments referencing this program are rejected (cross-context: BeneficiaryManagement reads `program.status`).
2. **Given** a `Suspended` program, **When** admin transitions to `Retired` (terminal), **Then** no more cycles can be triggered for it (FR-003 in 001).

### Edge Cases

- **Concurrent updates**: optimistic locking via `version`; second writer gets `409 Conflict`.
- **Adjustment factor out of plausible range** (`< -1` or `> 1`): `400 Bad Request`.
- **Effective date in the past on creation**: rejected unless `forceBackdate = true` (admin-only, audited).
- **Region code 99 in the UF table**: explicitly forbidden — region 99 is a beneficiary attribute, never a real UF (BR-024 / MYS-008).
- **Type change attempt** (`A → P`): not allowed on Active programs — would silently change BR-019 (Christmas allowance). Requires retire-and-recreate workflow.

## Requirements

### Functional (FR)

| ID | Requirement | Source |
|---|---|---|
| FR-001 | Operator MUST be able to register a program with `code, type ∈ {A,P,T}, baseAmount, adjustmentFactor, effectiveFrom`. | `CADPROG.NSN#L34-L72` |
| FR-002 | Program code is the natural key; the system MUST refuse duplicates. | `CADPROG.NSN#L41-L47` |
| FR-003 | The system MUST validate `type ∈ {A,P,T}` and reject other values. | `CADPROG.NSN#L77-L80` |
| FR-004 | The system MUST validate `adjustmentFactor` ∈ `[-1.0, 1.0]` (sanity range; legacy accepted anything). | `CADPROG.NSN#L81-L82` + workshop hardening |
| FR-005 | The system MUST preserve the K-factor formula `K = 1.00 + adj × 0.347215` (BR-010, MYS-003) as the consumer's contract. Adjustment factor is the input; the multiplication lives in PaymentCycle (`KFactor` class). | `CADPROG.NSN#L81-L82` |
| FR-006 | Operator MUST be able to read a program by code (`GET /api/v1/social-programs/{code}`). | `[GREENFIELD]` — legacy was read-only via terminal screen |
| FR-007 | Admin MUST be able to update the adjustment factor independently of other fields. | `CADPROG.NSN#L120-L131` |
| FR-008 | Admin MUST be able to replace the regional factor table atomically with a 27-UF map. | `BR-012` (legacy hardcoded in CALCBENF and BATCHPGT — BONUS-02) |
| FR-009 | Each regional factor MUST be in `[0.5, 2.0]` (sanity range). | `[GREENFIELD]` |
| FR-010 | `SocialProgramQueryPort.findActiveByCode(code)` MUST return `(code, type, baseAmount, adjustmentFactor, active)`. | published port for 001 |
| FR-011 | `SocialProgramQueryPort.regionalFactor(programCode, regionCode)` MUST return the factor for the given UF code. | published port for 001 |
| FR-012 | The system MUST refuse `regionCode = 99` in any regional factor table (MYS-008 — region 99 is a beneficiary bypass, never a real UF). | `VALELEG.NSN#L106-L110` |
| FR-013 | Admin MUST be able to transition program status `Active → Suspended → Retired`. | `CADPROG.NSN#L60-L65` |
| FR-014 | The system MUST emit `SocialProgramCreated`, `ProgramAdjustmentFactorChanged`, `RegionalFactorsReplaced`, `ProgramStatusChanged` domain events. | `[GREENFIELD]` — replaces legacy AUDITORIA writes |
| FR-015 | Every mutation MUST capture `before` and `after` snapshots in `audit_event` for 10-year retention (NFR-COMP-001). | Constitution Principle VI |

### Non-Functional (NFR)

| ID | Requirement |
|---|---|
| NFR-PERF-001 | `findActiveByCode` and `regionalFactor` MUST return in P95 ≤ 5 ms (cached read path, in-memory) |
| NFR-PERF-002 | Cache MUST invalidate within 1 s of an admin write (single-instance acceptable in MVP) |
| NFR-SEC-001 | All write endpoints MUST require `ADM` role |
| NFR-SEC-002 | `forceBackdate=true` MUST require `ADM` + non-empty `reason` and emit a HIGH-severity audit event |
| NFR-COMP-001 | All audit rows retained 10 years (LGPD/IN-TCU) |

### Constraints (CON)

| ID | Constraint |
|---|---|
| CON-001 | The K-factor constant `0.347215` does **not** live here — it lives in `KFactor` (PaymentCycle). This module only owns `adjustmentFactor`. |
| CON-002 | Regional factor table is per-program. There is no global table. |
| CON-003 | Type changes on an Active program are forbidden — see Edge Cases. |

### Out of Scope (OUT)

- Program-specific eligibility criteria DSL (renda max, idade min/max) — handled separately by an EligibilityValidation feature.
- Discount/contribution tables (`CALCDSCT`) — separate context.

## Key Entities

| Entity | Description | Key Fields |
|---|---|---|
| `SocialProgram` | Aggregate root. | `id`, `code`, `type`, `baseAmount`, `adjustmentFactor`, `status`, `effectiveFrom`, `effectiveUntil`, `version` |
| `RegionalFactor` | Per-program, per-UF multiplier. 27 rows per program. | `programId`, `ufCode`, `factor` |

## Success Criteria

1. **Wires 001**: with 003 in place, the PaymentCycle equivalence test runs end-to-end (T005 in 001/tasks).
2. **Cache hit rate** ≥ 99% on the read path during a cycle.
3. **Audit immutability** verified by ArchUnit + DB trigger.
4. **Adjustment factor change** propagates to the next cycle but not to in-flight ones (snapshot test).

## References

- Plan: [`plan.md`](plan.md) · Tasks: [`tasks.md`](tasks.md)
- Bounded context: [`../../02-spec-moderna/baseline/bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §3
- Business rules: BR-010, BR-012, BR-035
- Legacy: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN`
- Consumed by: feature 001 (`SocialProgramQueryPort`)
