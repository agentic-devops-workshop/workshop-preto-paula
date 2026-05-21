# Quickstart — Feature 004 Eligibility Validation

Five-minute path to verify the feature works end-to-end. Assumes feature 002 and 003 are also on `develop` (the validator depends on `beneficiary` and `social_program` tables).

## Prerequisites

- Backend running locally: `docker compose up -d` (Postgres) + `mvn spring-boot:run` in `03-implementacao/backend`
- At least one program registered (feature 003) and one beneficiary (feature 002). Seed via:

```bash
curl -X POST http://localhost:8080/api/v1/social-programs \
  -H 'Authorization: Bearer $ADM_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{ "code":"BFA1", "type":"A", "baseAmount":400.00, "adjustmentFactor":0.05, "effectiveFrom":"2026-06-01" }'
```

## 1. Unit + property tests (no Spring)

```bash
cd 03-implementacao/backend
mvn -Dtest='EligibilityValidator*' test
```

Expected: `Tests run: 13, Failures: 0, Errors: 0` (10 boundary + 3 property tests).

## 2. ArchUnit gate

```bash
mvn -Dtest=EligibilityArchitectureTest test
```

Expected green. Failure means a cross-module import sneaked in.

## 3. Simulate endpoint (manual smoke)

Eligible type-A:

```bash
curl -X POST http://localhost:8080/api/v1/eligibility/simulate \
  -H 'Authorization: Bearer $OPR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{ "programCode":"BFA1", "birthDate":"1990-05-14", "familyIncome":550, "dependents":2, "regionCode":26 }'
# → { "outcome": "Eligible" }
```

Ineligible type-A (income too high, no dependents):

```bash
curl ... -d '{ ..., "familyIncome":900, "dependents":0, "regionCode":26 }'
# → { "outcome": "Ineligible", "reason": "INCOME_AND_NO_DEPENDENTS", "detail": "income=900 max=600 dependents=0" }
```

Region-99 bypass:

```bash
curl ... -d '{ ..., "regionCode":99 }'
# → { "outcome": "EligibleByBypass", "regionCode": 99 }
# Server logs WARN: "Region-99 bypass evaluated. program=BFA1 regionCode=99"
```

## 4. Region-99 report (audit perspective)

After at least one payment cycle (feature 001) has run:

```bash
curl http://localhost:8080/api/v1/eligibility/region-99-report?cycleId=42 \
  -H 'Authorization: Bearer $AUD_TOKEN'
# → { "cycleId":42, "totalBeneficiaries":1247, "region99Count":18, ... }
```

CPFs are masked. To reveal:

```bash
curl ".../region-99-report?cycleId=42&revealCpf=true" -H 'Authorization: Bearer $AUD_TOKEN'
# → one audit event emitted per CPF revealed
```

## 5. Boot guard (FR-007)

Simulate a misconfigured production boot:

```bash
SPRING_PROFILES_ACTIVE=prod \
  SIFAP_ELIGIBILITY_REGION99BYPASS_ENABLED=false \
  mvn spring-boot:run
```

Expected: boot fails with `IllegalStateException` because the seeded beneficiary uses region 99 (if it does). Migrate those rows or re-enable the bypass.

## 6. Metrics

```bash
curl http://localhost:8080/actuator/prometheus | grep sifap_eligibility
# sifap_eligibility_evaluated_count_total{result="Eligible"} 12
# sifap_eligibility_evaluated_count_total{result="EligibleByBypass"} 1
# sifap_eligibility_region99_count_total{program="BFA1"} 1
```

## 7. Equivalence with legacy `VALELEG.NSN`

When feature 001's fixture lands on `develop`, run:

```bash
mvn -Dtest=EligibilityEquivalenceTest test
```

Asserts that for each row in `legacy-fixture-2026-05.csv`, this validator's accept/reject decision matches the captured legacy decision.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `PROGRAM_NOT_FOUND` for a code you just created | Feature 003 not merged on `develop` yet | Merge 003, restart backend |
| Boot fails with region-99 message | FR-007 guard triggered | Either re-enable bypass or migrate the offending rows out of region 99 |
| 403 on simulate | Missing JWT scope | Check token has any auth role |
| 403 on region-99 report | Missing AUD/ADM | Request role from your admin |
| Audit event count diverges from `payment.region_bypass` count | Bug — file an issue | This invariant is Success Criterion #1 |
