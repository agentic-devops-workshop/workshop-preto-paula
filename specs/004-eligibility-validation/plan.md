# Implementation Plan: Eligibility Validation

**Feature**: `004-eligibility-validation` · **Spec**: [`spec.md`](spec.md)
**Status**: Draft · 2026-05-20

## 1. Module Layout

```text
com.sifap.eligibility/
├── domain/
│   ├── EligibilityResult.java          ← sealed interface: Eligible | EligibleByBypass | Ineligible
│   ├── Reason.java                     ← enum (FR-011)
│   ├── EligibilityCriteria.java        ← record fed by SocialProgramRegistry
│   └── EligibilityValidator.java       ← pure function, the heart
├── application/
│   ├── EligibilityService.java         ← simulate, region-99 report aggregation
│   └── ports/
│       └── ProgramCriteriaPort.java    ← read-only port to SocialProgramRegistry
├── infrastructure/
│   ├── SpringEventAuditPublisher.java  ← publishes RegionBypassEvaluated
│   └── config/
│       └── Region99StartupGuard.java   ← FR-007 startup check
├── interfaces/
│   ├── EligibilityController.java      ← /simulate, /region-99-report
│   ├── EligibilityExceptionHandler.java
│   └── dto/...
└── api/
    └── BeneficiaryEligibilityPort.java ← PUBLISHED — consumed by 001 and 002
```

The aggregate's heart is `EligibilityValidator.validate(criteria, candidate)` — a 25-line pure function.

## 2. Domain Model

```java
public sealed interface EligibilityResult
    permits EligibilityResult.Eligible, EligibilityResult.EligibleByBypass, EligibilityResult.Ineligible {

    record Eligible() implements EligibilityResult {}
    record EligibleByBypass(short regionCode) implements EligibilityResult {}
    record Ineligible(Reason reason, String detail) implements EligibilityResult {}

    default boolean isEligible() { return !(this instanceof Ineligible); }
    default boolean isBypass()   { return this instanceof EligibleByBypass; }
}
```

`EligibilityValidator.validate` short-circuits on `regionCode == 99` (FR-005), then dispatches on program type.

## 3. Published Port

```java
public interface BeneficiaryEligibilityPort {
    EligibilityResult validate(String programCode, LocalDate birthDate,
                               BigDecimal familyIncome, int dependents, short regionCode);
}
```

## 4. Integration with Other Features

| Caller | When | Consequence on `Ineligible` |
|---|---|---|
| **002 BeneficiaryRegistration** | On `POST /beneficiaries` | Block registration with `422 Unprocessable Entity` + structured reason. |
| **001 PaymentCycle** | Per row in the processor | Skip the row, write to `cycle_skip` with reason. |
| **004 itself** | `/simulate` endpoint | Return result without persisting. |

Audit:

- `Eligible` → no event (high volume; covered by cycle-level metric).
- `EligibleByBypass` → `RegionBypassEvaluated` event at WARN (FR-006).
- `Ineligible` → no event from this module; the caller (registration or cycle) decides whether to audit at its level.

## 5. Region-99 Report (FR-008)

`GET /api/v1/eligibility/region-99-report?cycleId={id}` returns:

```json
{
  "cycleId": 42,
  "competence": "2026-06",
  "programCode": "BFA1",
  "totalBeneficiaries": 1247,
  "region99Count": 18,
  "region99Percent": 1.44,
  "beneficiaries": [
    { "maskedCpf": "XXX.XXX.XXX-09", "ageAtRun": 47, "lastUpdate": "2014-03-12" },
    ...
  ]
}
```

CPF is masked by default; `revealCpf=true` requires `ADM` or `AUD` role and emits one audit event per row revealed.

## 6. Startup Guard (FR-007)

```java
@Configuration
@Profile("prod")
class Region99StartupGuard {
    @PostConstruct
    void enforce(DataSource ds, @Value("${sifap.eligibility.region99Bypass.enabled:true}") boolean bypassEnabled) {
        if (!bypassEnabled) {
            long n = countRegion99Beneficiaries(ds);
            if (n > 0) throw new IllegalStateException(
                "FATAL: region-99 bypass is disabled but " + n + " beneficiaries still carry regionCode=99. " +
                "Migrate those records before disabling the bypass. See FR-007.");
        }
    }
}
```

## 7. Test Strategy

| Layer | Coverage |
|---|---|
| Validator unit | **100%**, including all reason branches and boundary ages (16, 18, 60, 65) and income thresholds |
| Validator property test | random inputs assert: never returns Eligible when income > max AND dependents = 0 AND type = A AND regionCode ≠ 99 |
| Region-99 bypass test | dedicated test asserting bypass overrides all rules |
| Service integration | Testcontainers + mocked ProgramCriteriaPort |
| API (RestAssured) | `/simulate` + `/region-99-report` + RBAC matrix |
| ArchUnit | only `api/BeneficiaryEligibilityPort` may be imported by other modules |
| Boot guard | `@SpringBootTest(profiles=prod, props=bypassEnabled=false)` + seeded region-99 row MUST fail to start |
| Performance | microbenchmark: 100k `validate` calls in ≤ 1s (≤ 10 µs each) |

## 8. Constitution Compliance

- I (Legacy Traceability): every FR cites `VALELEG.NSN` or `[GREENFIELD]`
- II (Modular Monolith): module is a sibling of SocialProgramRegistry; reads through `ProgramCriteriaPort`
- III (Test-First): tests cover boundaries before implementation
- IV (Stack Discipline): no new dependencies
- V (Single Source of Truth): age formula lives only in `EligibilityValidator` (matches the legacy BR-005 imprecision until Phase 2)
- VI (Security & Compliance): RBAC on reveal-CPF and reports; immutable audit on bypass
- VII (Observability): counters per result type, per program; bypass anomaly alert
