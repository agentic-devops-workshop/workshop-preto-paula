<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-004: FATOR-K Handling — Parameter Table with Audit, Not a Magic Constant

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY Critical](https://img.shields.io/badge/SEVERITY-Critical-F25022?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture
- Reviewed by: Par 1 · Vision (Product Owner) — financial calculation policy
- Reviewed by: Par 4 · Quality (DBA) — schema decision
- **Awaiting SENARC confirmation** — they originated the value but documentation was lost

## Technical Story

Stage 1 archaeology uncovered MYS-003 (★★★ critical): the constant `0.347215` is hardcoded in `CADPROG.NSN#L81-L82`:

```natural
* CALC VLR BASE AJUSTADO C/ FATOR K
COMPUTE #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)
COMPUTE #VLR-CALC = #VLR-BASE * #FATOR-K
```

The DDM `PROGRAMA-SOCIAL.ddm` field `BG FATOR-K` has an explicit comment from the DBA:

```text
>>> NAO DOCUMENTADO <<<
INSERIDO AGO/2008 POR ADILSON
"ATENDE SOLICITACAO SENARC"
SEM MAIS DETALHES NO CHAMADO
```

Origin unknown, retroactive impact unknown. Used in every social program registration since 2008.

If we get this wrong: every social program's base value will diverge by `~34.7% × adjustment_factor` from the legacy. Financial impact is unbounded.

## Context and Problem Statement

We need to handle `FATOR-K` in SIFAP 2.0 in a way that:

1. Preserves the exact legacy value (`0.347215`) for backward compatibility
2. Allows SENARC to update the value **without a code deploy** (the original change in 2008 was a SENARC request — likely to recur)
3. Records every change with audit trail (who changed it, when, why)
4. Allows different values per program if business needs evolve (some programs may eventually have different K factors)
5. Makes the formula traceable (anyone reading the code can find the value and its provenance)

The decision is **where to store** `FATOR-K` and **how to govern its change**.

## Decision Drivers

- **D1 — Preservation of legacy value**: must produce identical output to legacy on day 1
- **D2 — Change cadence**: SENARC may request adjustments without warning; deploy cycle should not block them
- **D3 — Auditability**: any change to a financial constant must be traceable (who, when, why, prior value)
- **D4 — Per-program flexibility**: different social programs may eventually use different K values
- **D5 — Discoverability**: a developer must be able to find this constant and its provenance in < 1 minute
- **D6 — Test stability**: unit tests must not break if the value is changed in production
- **D7 — Compliance**: TCU and federal auditors may request the value's provenance — must be persistently logged

## Considered Options

1. **Hardcoded constant** — `private static final BigDecimal FATOR_K = new BigDecimal("0.347215");` in `PaymentCalculatorService`
2. **Application configuration** — `application.yml` property `sifap.calculation.fator-k=0.347215`
3. **Database parameter table** — `program_calculation_parameter` table with FK to `social_program`, audited
4. **Feature flag** — toggled via LaunchDarkly/Azure App Configuration
5. **Per-program column** — `social_program.fator_k` column with default `0.347215`

## Decision

**Chosen option: 3 + 5 hybrid — Per-program column with default, audit-tracked via standard JPA Auditing.**

- **Schema**:

  ```sql
  ALTER TABLE social_program
    ADD COLUMN fator_k numeric(7, 6) NOT NULL DEFAULT 0.347215;
  ```

- **Override capability**: each row in `social_program` carries its own `fator_k`. New programs default to `0.347215` (legacy value preserved); SENARC can override per program via admin UI.
- **Audit**: standard `audit_event` table (FR-AUD-001) records every UPDATE on `social_program.fator_k` with previous and new values, user, timestamp, and a **mandatory `reason` text field** (UI rejects empty reason).
- **Provenance documentation**: column comment in PostgreSQL DDL explicitly references this ADR and MYS-003:

  ```sql
  COMMENT ON COLUMN social_program.fator_k IS
    'Adjustment factor applied to base value. Default 0.347215 = legacy value from SIFAP 1.0 (Aug 2008, SENARC request, original documentation lost). See ADR-004 and MYS-003.';
  ```

- **API access**: read-only `GET /api/v1/social-programs/{code}/fator-k` returns current value + last-changed timestamp + user
- **Calculation**: `PaymentCalculatorService` loads `fator_k` from the program entity on every calculation (no caching beyond JPA's session cache) → guarantees changes take effect on next payment cycle
- **Tests**: unit tests use explicit values via test fixtures (not the production value) → SENARC change doesn't break CI

## Pros and Cons of the Options

### Option 1 — Hardcoded constant

- ✅ Simplest possible implementation
- ✅ Trivially testable
- ❌ **Any change requires code deploy** + release + Cab approval — SENARC can't react quickly to policy change
- ❌ **No audit**: git blame works, but doesn't capture *why*
- ❌ **No per-program flexibility**: locks the value across all programs forever
- ❌ Repeats the legacy sin — magic constant in code with no governance

### Option 2 — Application configuration

- ✅ Change without recompile (still requires restart)
- ✅ Visible in `application.yml` (better than buried in Java)
- ❌ **Still requires deploy** (config is bundled with image)
- ❌ **No audit**: who changed `application.yml`? When? Why? Git diff isn't enough — the "why" lives in JIRA, not the file
- ❌ **No per-program flexibility**

### Option 3 — Database parameter table

- ✅ Change at runtime via admin UI (no deploy)
- ✅ Full audit trail via standard auditing infrastructure
- ✅ Reason field forces governance
- ❌ More tables to manage
- ❌ Requires admin UI work (mitigated by reusing existing program admin screens)
- ❌ Slight performance cost: extra DB lookup per calculation (mitigated by JPA L1 cache + the row already loaded for other fields)

### Option 4 — Feature flag

- ✅ Excellent for boolean toggles
- ❌ **Wrong tool for a numeric financial constant**: feature flags are designed for on/off, percentages, or A/B — not for a precise `numeric(7, 6)` that affects every calculation
- ❌ Adds external dependency (Azure App Configuration or similar)

### Option 5 — Per-program column

- ✅ Maximum flexibility (each program may have a different K)
- ✅ Schema is self-documenting via column comment
- ❌ Without an admin UI, changing the value requires SQL UPDATE — bypasses audit and validation
- ❌ Without a default constraint, new programs forget to set the value

### Option 3 + 5 hybrid (chosen)

Combines: per-program column (Option 5) for storage + flexibility, governed by parameter-table audit semantics (Option 3) for change governance and audit trail.

## Consequences

### Positive

- Legacy value (`0.347215`) preserved as default → equivalence test on day 1 passes
- SENARC can adjust the value at runtime via admin UI → no deploy needed for policy changes
- Every change is fully audited (who, when, prior, new, reason) → audit-ready for TCU
- Per-program override available when business demands → no more "one K to rule them all"
- Column comment + ADR reference + MYS-003 link → next developer finds the provenance in 30 seconds
- Unit tests use explicit fixtures → production value change doesn't break CI

### Negative

- **Schema migration required**: adding `fator_k` column to `social_program` + backfill all existing rows with `0.347215`
- **Admin UI work**: need a small form to edit + audit a change with mandatory reason (new requirement: FR-PROG-007 to be added)
- **Slight performance overhead**: one extra column read per calculation. Negligible at SIFAP scale.
- **Risk of misuse**: an operator could set `fator_k = 0` accidentally and zero-out every program. **Mitigation**: range validation (must be between 0.0 and 2.0 per Bean Validation `@DecimalMin/@DecimalMax`) + admin role required + audit alert if value changes by more than 50%

### Neutral

- Persistent storage of historical values: handled by `audit_event` table (no separate version table needed)

## Validation

- ✅ **Equivalence test**: load 100 random programs from a legacy DB snapshot, calculate adjusted base value via SIFAP 2.0 with `fator_k = 0.347215` → output matches legacy to last decimal
- ✅ **Migration test**: Flyway script applied to empty schema + seeded data → all programs have `fator_k = 0.347215`
- ✅ **Audit test**: change `fator_k` from `0.347215` to `0.4` via API → `audit_event` row exists with `before_state.fator_k = "0.347215"`, `after_state.fator_k = "0.4"`, `reason` non-empty, `user_id` populated
- ✅ **Range validation test**: attempt to set `fator_k = -0.5` → API returns 400 with validation error
- ✅ **UI test (Playwright)**: edit screen shows current value + "last changed by X at Y" + mandatory reason field; submit with empty reason → blocked
- ✅ **DB integrity test**: SQL `UPDATE social_program SET fator_k = ...` bypassing API → audit alert via Application Insights (Azure Monitor rule)

## Related Requirements

- FR-PROG-002: Apply Fator K on base value composition
- FR-PROG-007 *(new)*: Allow runtime update of `fator_k` per program with mandatory audit reason (to be added to requirements.md)
- BR-010: Fator K = `1.00 + (FATOR-REAJUSTE × 0.347215)`
- MYS-003: Magic constant origin unknown — preserved as default, governance added
- NFR-COMP-002: 10-year audit retention covers FATOR-K changes
- NFR-SEC-007: Only ADM role can update `fator_k` (RBAC)

## References

- [`techstack.md`](techstack.md) — section 6, decision #3
- [`mysteries-found.final.md`](../../01-arqueologia/output-requisitos/mysteries-found.final.md) — MYS-003
- [`business-rules-catalog.final.md`](../../01-arqueologia/output-requisitos/business-rules-catalog.final.md) — BR-010
- DDM `PROGRAMA-SOCIAL.ddm` field `BG FATOR-K` comment (legacy)
- Spring Data JPA Auditing — [official docs](https://docs.spring.io/spring-data/jpa/reference/auditing.html)
- Bean Validation `@DecimalMin/@DecimalMax`

---