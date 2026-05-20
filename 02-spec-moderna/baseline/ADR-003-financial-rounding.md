<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003: Financial Rounding Policy — Half-Even with Legacy Compatibility Flag

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY Critical](https://img.shields.io/badge/SEVERITY-Critical-F25022?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture (Enterprise Architect + Software Architect)
- Reviewed by: Par 1 · Vision (Product Owner) — financial impact decision
- Reviewed by: Par 4 · Quality (DBA) — affects database `numeric(p,s)` strategy
- **Open for SENARC confirmation** — financial regulator must validate before production cut-over

## Technical Story

Stage 1 archaeology identified two contradictory rounding methods in the legacy SIFAP:

- **MYS-005** (Critical): All `CALC*` programs (CALCBENF, CALCDSCT, CALCCORR) use **truncation** via the mainframe idiom `VLR-TEMP = VLR * 100; VLR = VLR-TEMP / 100` (integer division). Always rounds **down**.
- **INC-004** (Critical): `BATCHREL.NSN` uses **half-up** via `VLR + 0.005` with an explicit comment `NOTA: ARREDONDAMENTO DIFERE DO CALCBENF`. Two different methods for the same money values.

Estimated production impact: at 180M records/month × ~R$ 0.005 average difference = **~R$ 900,000/month** discrepancy if we change the method blindly.

This is the most financially sensitive ADR. The decision affects every beneficiary, every month.

## Context and Problem Statement

We need a single, consistent rounding method for SIFAP 2.0 that:

1. Is mathematically defensible (no systematic bias)
2. Aligns with Brazilian financial regulations and accounting standards
3. Allows a safe cut-over from the legacy method without sudden discrepancies that beneficiaries would notice and contest
4. Eliminates the legacy inconsistency (calculation method ≠ report method)

The core tension: **preserving legacy behavior** (no surprise to beneficiaries who got used to the truncated values) vs **fixing a bug** (truncation systematically takes from beneficiaries, violating fairness).

## Decision Drivers

- **D1 — Mathematical correctness**: rounding should not bias systematically toward or against beneficiaries
- **D2 — Regulatory compliance**: Brazilian Central Bank (BACEN) and most accounting standards prefer half-even (also called "banker's rounding") for financial calculations
- **D3 — Beneficiary experience**: payments should not suddenly jump (up or down) in the cut-over month
- **D4 — Auditability**: rounding method must be explicit and testable, not implicit in a low-level integer division
- **D5 — Reversibility**: if half-even causes unforeseen issues, we must be able to switch back without code change
- **D6 — Volume**: 180M records/month means even a R$ 0.001 systematic difference is material

## Considered Options

1. **Preserve legacy truncation** — replicate the mainframe `* 100 / 100` integer division everywhere
2. **Adopt half-even (banker's rounding)** — `BigDecimal.setScale(2, RoundingMode.HALF_EVEN)` everywhere, immediate cut-over
3. **Half-even with compatibility flag** — default to half-even; feature flag `sifap.rounding.mode = TRUNCATE | HALF_EVEN` allows fallback for parallel-run period
4. **Configurable per bounded context** — different methods per context (e.g., truncation in PaymentProcessing, half-even in Reporting)

## Decision

**Chosen option: 3 — Half-even with compatibility flag.**

- **Default for SIFAP 2.0**: `RoundingMode.HALF_EVEN` (banker's rounding), 2 decimal places, applied to every monetary `BigDecimal` operation at the boundary of `PaymentCalculatorService`
- **Compatibility flag** `sifap.rounding.mode` with values:
  - `HALF_EVEN` (default in production, new system) — IEEE 754 banker's rounding
  - `LEGACY_TRUNCATE` (opt-in for parallel run period only) — replicates the mainframe truncation exactly, byte-for-byte equivalent to CALCBENF
- **Cut-over plan**:
  1. Month M-1: deploy with `LEGACY_TRUNCATE` enabled → identical output to legacy
  2. Month M: switch to `HALF_EVEN`, publish official notice (every beneficiary's monthly statement gets a note about the rounding adjustment policy)
  3. Month M+3: remove `LEGACY_TRUNCATE` code path (delete the flag)
- **Single rounding boundary**: rounding happens **only** in `PaymentCalculatorService.round()`. Intermediate calculations use full `BigDecimal` precision. Storage uses `numeric(11, 2)`.
- **Reporting context** also uses `HALF_EVEN` — eliminates INC-004 (no more divergence between calculation and report)

## Pros and Cons of the Options

### Option 1 — Preserve legacy truncation

- ✅ Zero discrepancy with legacy on day 1 — no beneficiary complaint
- ✅ Easiest to validate via equivalence test (run same input through legacy and new, expect identical output)
- ❌ **Perpetuates a known bug**: MYS-005 systematically favors the government over beneficiaries. We'd be migrating a defect.
- ❌ **Doesn't fix INC-004**: BATCHREL still uses half-up; we'd need to align by making reports also truncate, which means reports diverge from any modern accounting reconciliation tool
- ❌ **Future audit risk**: if TCU ever questions the rounding method, the answer "we kept it because the legacy did" doesn't hold
- ❌ **Locks in non-standard behavior**: any future integration with banking systems (Pix, Open Banking) will hit the half-even default and disagree with SIFAP

### Option 2 — Adopt half-even immediately

- ✅ Mathematically correct from day 1
- ✅ Eliminates both MYS-005 and INC-004
- ✅ Aligns with BACEN and IFRS conventions
- ❌ **Sudden discrepancy at cut-over**: beneficiaries who received R$ 100.56 for years suddenly get R$ 100.57 (or vice versa). Even small differences trigger complaints in a 4.2M user base.
- ❌ **No rollback path** if integration with downstream systems (CNAB return, SIAFI) breaks: we'd be stuck redeploying under pressure
- ❌ **Hard to validate via equivalence test**: output won't match legacy, requires separate validation criteria

### Option 3 — Half-even with compatibility flag (chosen)

- ✅ Day-1 cut-over runs `LEGACY_TRUNCATE` → equivalence test passes, no surprise
- ✅ Gradual transition with explicit operator decision and beneficiary notice
- ✅ Both MYS-005 and INC-004 fixed in the same release (once `HALF_EVEN` is on)
- ✅ Reversible: flip the flag if anything breaks
- ✅ Tests cover both modes — we know exactly what each produces
- ❌ **Two code paths to maintain** for the transition window (mitigation: flag is removed at M+3)
- ❌ **Operational discipline required** to actually flip the flag and remove it (mitigation: track in a sunset issue with a hard deadline)

### Option 4 — Configurable per bounded context

- ❌ **Recreates INC-004 by design**: calculation and report contexts using different methods is exactly what we want to eliminate
- ❌ **Cognitive overhead**: developers must remember which context uses which mode
- ❌ Rejected immediately

## Consequences

### Positive

- Single, defensible rounding policy across all monetary calculations
- Cut-over is **safe** (matches legacy on day 1) and **reversible** (flag flip)
- Both MYS-005 (systematic loss) and INC-004 (report ≠ calc) eliminated
- Easy to write equivalence tests against legacy: `LEGACY_TRUNCATE` mode → same output as Natural `* 100 / 100`
- Reporting becomes trustworthy (matches calculation result, no R$ 0.005 surprise)
- Audit trail records the active mode per payment (column `rounding_mode` in `payment` table) — explainability for any future regulator question

### Negative

- **Temporary R$ 900K/month adjustment** when switching from `LEGACY_TRUNCATE` to `HALF_EVEN`. Half the beneficiaries receive ~R$ 0.005 more on average. **This is a feature, not a bug**: it corrects 29 years of systematic under-payment.
- **Operational discipline required**: flag must actually be flipped, and the legacy code path must actually be removed by M+3
- **Two test suites** during transition: one for `LEGACY_TRUNCATE` equivalence, one for `HALF_EVEN` correctness
- Slightly more complex `PaymentCalculatorService` (one extra `if (mode == LEGACY_TRUNCATE)` branch)

### Neutral

- Storage stays the same: `numeric(11, 2)` works for both modes
- API response format unchanged

## Validation

- ✅ **Equivalence test (LEGACY_TRUNCATE mode)**: run 10,000 randomized beneficiary calculations through the new system in `LEGACY_TRUNCATE` mode AND through a faithful Java translation of `CALCBENF.NSN`. Output must match to the last decimal.
- ✅ **Correctness test (HALF_EVEN mode)**: 50+ unit tests covering edge cases:
  - Exactly R$ X.005 → rounds to even (R$ X.00 or X.01 depending on preceding digit)
  - R$ X.004 → rounds down
  - R$ X.006 → rounds up
  - Negative amounts behave symmetrically
- ✅ **Report ↔ calculation reconciliation test**: generate 10,000 payments, sum gross value via `PaymentCalculatorService`, run report aggregation — both totals must match exactly (eliminates INC-004)
- ✅ **Performance test**: switching modes adds ≤ 0.1% to calculation latency
- ✅ **Audit test**: every persisted `payment` row carries `rounding_mode` column with the active value at creation time
- ✅ **Cut-over runbook documented** in `02-spec-moderna/runbooks/rounding-cutover.md` (out of scope for ADR but referenced)

## Related Requirements

- FR-PAY-008: Apply rounding method defined in ADR-003 (default: half-even with legacy flag)
- BR-020, BR-021: Source business rules (legacy truncation + legacy half-up)
- INC-004: Two rounding methods divergence — resolved by this ADR
- MYS-005: Systematic loss of cents — resolved by switching to HALF_EVEN
- NFR-PERF-001/002/003: Performance budget allows the rounding overhead
- NFR-COMP-001: LGPD compliance — beneficiaries get a notice about the change

## References

- [`techstack.md`](techstack.md) — section 6, decision #2
- [`mysteries-found.final.md`](../../01-arqueologia/output-requisitos/mysteries-found.final.md) — MYS-005, INC-004
- [`business-rules-catalog.final.md`](../../01-arqueologia/output-requisitos/business-rules-catalog.final.md) — BR-020, BR-021
- IEEE 754-2019 — banker's rounding spec
- [Java `RoundingMode.HALF_EVEN` javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/RoundingMode.html#HALF_EVEN)
- BACEN Carta Circular nº 3,565/2012 — rounding conventions for financial systems (Brazilian Central Bank)
- Martin Fowler, *Patterns of Enterprise Application Architecture* — "Money" pattern

---
