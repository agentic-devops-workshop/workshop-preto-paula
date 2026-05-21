# Phase 1 — Data Model

**Feature**: 004 Eligibility Validation · **Date**: 2026-05-20

This feature **does not own any new database table**. It is a read-only consumer of state owned by features 002 (`beneficiary`) and 003 (`social_program`). All "entities" here are in-memory value objects.

## In-memory model

### `EligibilityResult` (sealed interface)

```text
sealed interface EligibilityResult permits Eligible, EligibleByBypass, Ineligible

record Eligible()
record EligibleByBypass(short regionCode)
record Ineligible(Reason reason, String detail)
```

**Invariants**:

- An `EligibleByBypass` always carries `regionCode == 99` (FR-005).
- `Ineligible.reason` is never `null`.
- `Ineligible.detail` is optional (`null` allowed) and used for human-readable context, not for control flow.

### `Reason` (enum)

| Value | When |
|---|---|
| `INCOME_AND_NO_DEPENDENTS` | Type A: income > max AND dependents = 0 (BR-025) |
| `AGE_BELOW_60` | Type P: age < 60 (BR-026) |
| `AGE_BELOW_16` | Type T: age < 16 (BR-027 lower bound) |
| `AGE_ABOVE_65` | Type T: age > 65 (BR-027 upper bound) |
| `PROGRAM_NOT_FOUND` | Program code unknown to feature 003 |
| `PROGRAM_RETIRED` | Program in terminal status |
| `INVALID_INPUT` | Negative income, negative dependents, null required field |

### `EligibilityCriteria` (record fed by `ProgramCriteriaPort`)

| Field | Type | Source (feature 003) | Notes |
|---|---|---|---|
| `programCode` | `String(4)` | `social_program.code` | Natural key |
| `type` | `char` | `social_program.type` | 'A', 'P' or 'T' |
| `incomeMax` | `BigDecimal` | `social_program.income_max` | Used only for type A; `null` for P/T |
| `active` | `boolean` | `social_program.status = 'Active'` | informational |
| `retired` | `boolean` | `social_program.status = 'Retired'` | terminal short-circuit |

### Validator inputs (parameters of `validate`)

| Parameter | Type | Source |
|---|---|---|
| `criteria` | `EligibilityCriteria` | resolved by caller from `ProgramCriteriaPort` |
| `birthDate` | `LocalDate` | `beneficiary.birth_date` (feature 002) |
| `familyIncome` | `BigDecimal` | `beneficiary.family_income` (feature 002) |
| `dependents` | `int` | derived from `beneficiary.dependent_count` (feature 002) |
| `regionCode` | `short` | `beneficiary.region_code` (feature 002) |
| `referenceDate` | `LocalDate` | caller-supplied (R-001) |

## Persistence touchpoints (read-only)

| Table | Owner feature | Columns this feature reads | Use |
|---|---|---|---|
| `beneficiary` | 002 | `cpf`, `birth_date`, `family_income`, `dependent_count`, `region_code` | Region-99 startup probe (FR-007); region-99 report (FR-008) |
| `social_program` | 003 | `code`, `type`, `income_max`, `status` | Resolved into `EligibilityCriteria` |
| `payment` | 001 | `cpf`, `region_bypass`, `cycle_id`, `competence`, `program_code` | Region-99 report by `cycleId` (R-008) |

No `INSERT`/`UPDATE`/`DELETE` from this feature on any table. ArchUnit enforces this through a rule that forbids `@Modifying` annotations and `EntityManager.persist` / `merge` calls inside `..eligibility..`.

## Audit emission

| Event | Trigger | Severity | Consumer |
|---|---|---|---|
| `RegionBypassEvaluated(programCode, regionCode, occurredAt)` | Every `EligibleByBypass` result returned by `DefaultEligibilityPortAdapter.validate(...)` | `WARN` | `ReportingAndAudit` context writes immutable `audit_event` row (10-year retention per NFR-COMP-001) |

No other events emitted by this feature.

## State transitions

This feature has **no aggregates with state**. The only state lives in `social_program.status` (owned by 003), which transitions `Active → Suspended → Retired` per CL-002 in 003's spec.

## Validation rules (summary, see plan §Constitution Check III for tests)

1. `birthDate != null AND familyIncome != null AND referenceDate != null` else `INVALID_INPUT`.
2. `familyIncome.signum() >= 0 AND dependents >= 0` else `INVALID_INPUT`.
3. `regionCode ∈ {1..26, 99}` else `INVALID_INPUT` with detail `"regionCode=N out of accepted range"` (clarification 2026-05-21).
4. `regionCode == 99` ⇒ `EligibleByBypass(99)` (overrides 5–7).
5. `criteria == null` ⇒ `PROGRAM_NOT_FOUND`.
6. `criteria.retired == true` ⇒ `PROGRAM_RETIRED`.
7. Type-specific rule per `criteria.type` (BR-025/026/027).
