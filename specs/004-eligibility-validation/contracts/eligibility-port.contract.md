# Internal Port Contract — `BeneficiaryEligibilityPort`

**Package**: `com.sifap.eligibility.api`
**Consumers**: feature 001 (PaymentCycle), feature 002 (BeneficiaryRegistration)

## Method

```java
EligibilityResult validate(
    String programCode,
    LocalDate birthDate,
    BigDecimal familyIncome,
    int dependents,
    short regionCode
);
```

## Inputs

| Parameter | Required | Validation |
|---|---|---|
| `programCode` | yes | non-null; 4 characters `[A-Z0-9]{4}`; consumer must enforce — port returns `PROGRAM_NOT_FOUND` if missing |
| `birthDate` | yes | non-null; not after the reference date; consumer enforces presence |
| `familyIncome` | yes | non-null; `signum() >= 0`; otherwise `Ineligible(INVALID_INPUT)` |
| `dependents` | yes | `>= 0`; otherwise `Ineligible(INVALID_INPUT)` |
| `regionCode` | yes | `short`; `99` triggers bypass; any other value treated as a real region |

The reference date is **not** a port parameter. Each caller chooses its own clock source (registration: today; cycle: `cycle.snapshotAt`) inside its own adapter wrapper. This keeps the port surface small and the validator pure (R-001).

## Outputs

One of:

- `EligibilityResult.Eligible`
- `EligibilityResult.EligibleByBypass(short regionCode)` — always emits `RegionBypassEvaluated` audit event (FR-006)
- `EligibilityResult.Ineligible(Reason reason, String detail)`

## Side effects

- One audit event published per `EligibleByBypass` result.
- One Micrometer counter increment per call (`sifap.eligibility.evaluated.count{result=...}`).
- No DB writes; no I/O outside in-process events.

## Error semantics

The port **does not throw** for any input. Invalid input becomes `Ineligible(INVALID_INPUT)`. Spring's `@RestControllerAdvice` translates the result into HTTP responses at the caller's controller layer; the port itself does not know about HTTP.

## Compatibility & versioning

This port has **no version**. Breaking changes are forbidden — any new parameter must be added as a backward-compatible overload. If business rules need a richer input (e.g. `regionFamilyComposition`), introduce a sibling port `BeneficiaryEligibilityPortV2` and deprecate the old one with a 3-month transition window.

## Contract test

`SocialProgramPortContractTest` (feature 003) plus `BeneficiaryEligibilityPortContractTest` (this feature) verify:

1. The signature exposed by the producing module matches the signature expected by each consumer module byte-for-byte (using reflection).
2. The result type's sealed permits list matches across modules.

The test runs in CI on every PR to `develop`. A signature mismatch fails the build.

## ArchUnit guard

Cross-module imports are restricted by `EligibilityArchitectureTest`:

```text
no classes outside com.sifap.eligibility..
  may import any class in com.sifap.eligibility.{domain, application, infrastructure, interfaces}..
  except classes in com.sifap.eligibility.api..
```
