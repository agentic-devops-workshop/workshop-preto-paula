# Quickstart Evidence — Feature 004 Eligibility Validation

Generated locally for T033 after composing features 001, 002, 003, and 004 in the scratch runtime worktree `/tmp/sifap-worktrees/runtime-composition-20260521125417`.

## Automated Checks Executed

- `mvn -q test` in `03-implementacao/backend`: PASS after adding the reference-date overload used by payment cycle snapshot semantics.
- `mvn -q test` in `/tmp/sifap-worktrees/002-beneficiary-registration/03-implementacao/backend`: PASS after wiring `BeneficiaryService.register(...)` to `BeneficiaryEligibilityPort`.
- `mvn -q test` in `/tmp/sifap-worktrees/001-payment-cycle-generation/03-implementacao/backend`: PASS after wiring the payment cycle processor to `BeneficiaryEligibilityPort` and recording `cycle_skip` rows.
- `mvn -q test` in `/tmp/sifap-worktrees/003-social-program-registry/03-implementacao/backend`: PASS after adding `income_max` and offset-aware JPA auditing.
- `mvn -q clean test` in `/tmp/sifap-worktrees/runtime-composition-20260521125417/03-implementacao/backend`: PASS (`MAVEN_RUNTIME_COMPOSITION14=PASS`).

## Runtime Context

- Database: PostgreSQL 16 container `sifap-runtime-postgres`, exposed on `127.0.0.1:55432`.
- Backend: `mvn spring-boot:run` with `DB_URL=jdbc:postgresql://localhost:55432/sifap`, local HS256 JWT secret, and `SPRING_QUARTZ_AUTO_STARTUP=false`.
- Startup: Tomcat on `http://localhost:8080`; Flyway applied `V001`, `V002`, and `V003`; `/actuator/health` returned `200` with `{"status":"UP"}`.

## Manual Curl Smoke

Captured output is in `/tmp/sifap-quickstart/t033-http-smoke.md`. Key results:

### Seed BFA1 social program

```text
HTTP/1.1 201
Location: /api/v1/social-programs/BFA1
{"id":1,"code":"BFA1","type":"A","baseAmount":400.00,"adjustmentFactor":0.05,"status":"Active","effectiveFrom":"2026-06-01","effectiveUntil":null,"version":0}
```

### Simulate eligible

```text
HTTP/1.1 200
{"outcome":"Eligible","regionCode":null,"reason":null,"detail":null}
```

### Simulate ineligible

```text
HTTP/1.1 200
{"outcome":"Ineligible","regionCode":null,"reason":"INCOME_AND_NO_DEPENDENTS","detail":"income=900 max=600.00 dependents=0"}
```

### Simulate region-99 bypass

```text
HTTP/1.1 200
{"outcome":"EligibleByBypass","regionCode":99,"reason":null,"detail":null}
```

### Region-99 reports

```text
HTTP/1.1 200
{"cycleId":null,"competence":"2026-06","programCode":"BFA1","totalBeneficiaries":1,"region99Count":0,"region99Percent":0.0,"beneficiaries":[]}

HTTP/1.1 200
{"cycleId":42,"competence":null,"programCode":null,"totalBeneficiaries":1,"region99Count":0,"region99Percent":0.0,"beneficiaries":[]}
```

### Prometheus eligibility metrics

The actuator endpoint is protected in the composed runtime; unauthenticated `/actuator/prometheus` returned `401`, and the authenticated operator scrape returned `200` with:

```text
sifap_eligibility_evaluated_count_total{application="sifap-backend",result="Eligible"} 1.0
sifap_eligibility_evaluated_count_total{application="sifap-backend",result="EligibleByBypass"} 1.0
sifap_eligibility_evaluated_count_total{application="sifap-backend",result="Ineligible"} 1.0
sifap_eligibility_region99_count_total{application="sifap-backend",program="BFA1"} 1.0
```

## T033 Verdict

PASS. The quickstart was executed end-to-end against a running composed app with real local JWTs, seeded social-program data, eligibility simulation outcomes, region-99 report calls, and metrics evidence.

T033 should remain open until 001, 002, 003, and 004 are composed on `develop` and a running app with real local tokens is available.
