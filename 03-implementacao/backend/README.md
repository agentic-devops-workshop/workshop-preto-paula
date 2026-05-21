# 03-implementacao — Java backend (starter scaffolding)

This directory holds the modular monolith for SIFAP 2.0 — produced during Stage 3 (Builder) and used as the canonical reference by feature specs in `specs/`.

## What is here today (`/speckit.implement` v1)

| Module | Path | Status |
|---|---|---|
| `paymentprocessing` calculation core | `backend/src/main/java/com/sifap/paymentprocessing/domain/calculation/` | ✅ pure domain functions (Money, Competence, MainframeTruncation, KFactor, RegionalFactor, FamilyFactor, IncomeFactor, AgeFactor, ChristmasAllowance, PaymentFormula) |
| `paymentprocessing` migration | `backend/src/main/resources/db/migration/payment/V001__payment_cycle_init.sql` | ✅ Flyway V001 |
| `beneficiary` CPF primitives | `backend/src/main/java/com/sifap/beneficiary/domain/` | ✅ Cpf, BrazilianCpfMasker, BrazilianCpfValidator, CpfValidationStatus |
| `beneficiary` migration | `backend/src/main/resources/db/migration/beneficiary/V001__beneficiary_module_init.sql` | ✅ Flyway V001 |
| Unit tests | `backend/src/test/java/...` | ✅ `PaymentFormulaTest`, `BrazilianCpfValidatorTest` |

## What is **not** here yet (next PRs)

- `pom.xml` (Spring Boot 3.3 + Spring Batch + Quartz + Flyway 10 + Testcontainers + ArchUnit + AssertJ)
- JPA entities (`Beneficiary`, `Dependent`, `Payment`, `PaymentCycle`) — domain classes only at this stage
- Repositories, services, controllers, DTOs
- Spring Batch job (`PaymentCycleJobConfig`)
- ArchUnit suite
- Equivalence harness against the legacy fixture (the Stage 3 showcase)
- `application.yml`, `docker-compose.yml` wiring

These are the next tasks from `specs/001-.../tasks.md` and `specs/002-.../tasks.md` (T004 onwards).

## How to run (once `pom.xml` lands)

```bash
cd 03-implementacao/backend
mvn test                                    # unit tests
mvn test -Dtest=PaymentCycleEquivalenceTest # equivalence vs legacy fixture
```

## Conventions

- Java 21 LTS, sealed classes / records welcome.
- Constructor injection only — no `@Autowired` on fields.
- All monetary math goes through `Money` (RoundingMode.DOWN, BR-020).
- `RoundingMode.DOWN` is banned everywhere except `MainframeTruncation` — enforced by ArchUnit.
- CPF rendered via `BrazilianCpfMasker` only (ADR-007). Raw digits never leak to logs.
- Every new file has a class-level Javadoc citing the FR/BR it implements.
