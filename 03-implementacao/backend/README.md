# 03-implementacao — Java backend (modular monolith)

SIFAP 2.0 backend. Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Flyway + Spring Batch + Quartz.

## Module layout

```text
com.sifap
├── beneficiary/                ← Bounded context (feature 002)
│   ├── domain/                 ← Entities, value objects, enums, CPF rules
│   ├── application/            ← BeneficiaryService (transactional boundary)
│   ├── infrastructure/         ← JPA repositories, boot guards, config
│   └── interfaces/             ← REST controllers, DTOs
├── paymentprocessing/          ← Bounded context (feature 001)
│   ├── domain/
│   │   └── calculation/        ← Pure functions: Money, KFactor, PaymentFormula, …
│   ├── application/
│   ├── infrastructure/         ← Batch jobs (TODO), scheduler boot guard, persistence
│   └── interfaces/             ← (TODO) REST controllers
├── shared/
│   └── error/                  ← RFC 7807 global exception handler
└── SifapApplication.java       ← Spring Boot entry point
```

## What's in place

| Layer | Files | Status |
|---|---|---|
| Build | `pom.xml` | ✅ Spring Boot 3.3 + Spring Batch + Quartz + Flyway 10 + Testcontainers + ArchUnit |
| App bootstrap | `SifapApplication.java`, `application.yml` | ✅ |
| Migrations | `db/migration/{beneficiary,payment}/V001*.sql` | ✅ |
| `beneficiary` domain | `Cpf`, `BrazilianCpfValidator`, `BrazilianCpfMasker`, `Beneficiary`, `Dependent`, `Address`, enums | ✅ |
| `beneficiary` repository + service | `BeneficiaryRepository`, `BeneficiaryService` | ✅ |
| `beneficiary` REST | `BeneficiaryController`, DTOs | ✅ POST / GET / PATCH status / DELETE (soft) |
| `paymentprocessing` calculation | `PaymentFormula`, `MainframeTruncation`, `KFactor`, factor classes, `Money`, `Competence` | ✅ pure functions |
| `paymentprocessing` entities | `PaymentCycle`, `Payment`, enums | ✅ |
| `paymentprocessing` repository | `PaymentCycleRepository` | ✅ |
| Boot guards | `CpfBackdoorBootGuard` (ADR-008), `CycleSchedulerBootGuard` (FR-030) | ✅ |
| Error handling | `GlobalExceptionHandler` (RFC 7807) | ✅ |
| Tests | `PaymentFormulaTest`, `BrazilianCpfValidatorTest`, `LifecycleStatusTest`, `ModularMonolithArchitectureTest` | ✅ |

## What's next (tasks remaining)

- `paymentprocessing.application.PaymentCycleService` (trigger / cancel / resume)
- `paymentprocessing.infrastructure.batch.PaymentCycleJobConfig` (Spring Batch job — ADR-005)
- `paymentprocessing.interfaces.PaymentCycleController`
- Domain events + `@EventListener` to persist audit rows
- Equivalence harness `PaymentCycleEquivalenceTest` against `legacy-fixture-2026-05.csv` (the workshop showcase)
- Entra ID OAuth2 wiring + role mapping
- `docker-compose.yml` profile for the workshop demo

## Running

```bash
# Requires Java 21 and Postgres 16 reachable on localhost:5432
cd 03-implementacao/backend
mvn test                                    # unit + ArchUnit
mvn spring-boot:run                         # local dev profile
mvn test -Dtest=PaymentCycleEquivalenceTest # (when fixture lands)
```

## Conventions (enforced)

- Constructor injection only. `@Autowired` on fields is forbidden by code review.
- All monetary math goes through `Money`. `RoundingMode.DOWN` is allowed only in `MainframeTruncation` — ArchUnit gates this.
- CPF is rendered via `BrazilianCpfMasker` everywhere except the audited reveal endpoint.
- Module boundaries are enforced by `ModularMonolithArchitectureTest`.
- Every new class has a one-line Javadoc citing the FR / BR / ADR it implements.
