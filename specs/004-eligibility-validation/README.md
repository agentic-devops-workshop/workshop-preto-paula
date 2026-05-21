# Feature 004 — Eligibility Validation

Feature 004 replaces the legacy `VALELEG.NSN` validator with a pure Java rule engine and a small REST surface for simulation and region-99 oversight.

## What It Owns

- BR-024 / MYS-008: region-code 99 bypass, now exposed as `EligibleByBypass(99)` and audited with `RegionBypassEvaluated`.
- BR-025: type-A eligibility, `income <= incomeMax OR dependents >= 1`.
- BR-026: type-P eligibility, age at reference date must be at least 60.
- BR-027: type-T eligibility, age at reference date must be between 16 and 65 inclusive.
- Structured ineligibility reasons through the `Reason` enum.

## Public Contract

The only cross-module Java surface is `com.sifap.eligibility.api.BeneficiaryEligibilityPort`.

REST endpoints are intentionally operator-facing only:

- `POST /api/v1/eligibility/simulate`
- `GET /api/v1/eligibility/region-99-report`

## Branch Discipline

This branch implements feature-004-owned code only. Tasks T013 and T015 are cross-branch work and must be opened separately against features 002 and 001 after 004 lands on `develop`.

## Validation

Run from `03-implementacao/backend`:

```bash
mvn test
```

The current suite covers validator boundaries, property-based invariants, adapter audit/metrics behavior, rate limiting, controller authorization checks, region-99 startup guard, architecture rules, and a legacy equivalence fixture.

## References

- Legacy source: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN`
- Spec: `spec.md`
- Plan: `plan.md`
- Tasks: `tasks.md`
- Contracts: `contracts/`