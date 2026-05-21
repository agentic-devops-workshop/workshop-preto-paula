# Implementation Plan: Beneficiary Registration

**Feature Branch**: `002-beneficiary-registration`
**Spec**: [`spec.md`](spec.md)
**Created**: 2026-05-20
**Status**: Draft

> This plan honors [`baseline/techstack.md`](../../02-spec-moderna/baseline/techstack.md), [`baseline/bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md), and the constitution at `.specify/memory/constitution.md` v1.0.0.

## 1. Module Layout (package-by-feature, ADR-001)

```text
backend/src/main/java/com/sifap/beneficiary/
├── domain/
│   ├── Beneficiary.java                  ← aggregate root (entity)
│   ├── Dependent.java                    ← child entity (FK to Beneficiary)
│   ├── LifecycleStatus.java              ← enum (Active/Suspended/Cancelled/Inactive/Disabled)
│   ├── AgeCategory.java                  ← enum (Adult/Senior/Minor)
│   ├── CpfValidationStatus.java          ← enum (VALID/LEGACY_BACKDOOR/TEST)
│   ├── Parentage.java                    ← enum (Spouse/Child/Sibling/Other)
│   ├── Address.java                      ← embeddable value object
│   ├── Cpf.java                          ← value object with validation
│   ├── BrazilianCpfValidator.java        ← Mod-11 strict + backdoor-aware (ADR-008)
│   ├── BrazilianCpfMasker.java           ← masking utility (ADR-007)
│   └── events/
│       ├── BeneficiaryRegistered.java
│       ├── BeneficiaryStatusChanged.java
│       ├── DependentAdded.java
│       ├── DependentRemoved.java
│       └── AgeCategoryChanged.java
├── application/
│   ├── BeneficiaryService.java           ← orchestrates use cases
│   ├── DependentService.java
│   ├── AgeCategoryRecomputeJob.java      ← Spring @Scheduled daily 02:00
│   ├── CpfRevealService.java             ← privileged full-CPF access with audit
│   └── ports/
│       ├── SocialProgramQueryPort.java   ← read-only port to SocialProgramRegistry
│       └── AuditEventPublisher.java      ← publishes events for ReportingAndAudit
├── infrastructure/
│   ├── persistence/
│   │   ├── BeneficiaryRepository.java    ← Spring Data JPA
│   │   ├── DependentRepository.java
│   │   └── BeneficiaryQueryRepository.java
│   ├── messaging/
│   │   └── SpringEventAuditPublisher.java← in-process event publisher (ADR-001)
│   └── config/
│       ├── BeneficiaryModuleConfig.java
│       └── CpfBackdoorBootGuard.java     ← @PostConstruct refuses prod with flag=true (ADR-008)
├── api/                                  ← published interface (read-only ports for other contexts)
│   └── BeneficiaryQueryPort.java         ← findByCpf, findByNis, exists
└── interfaces/                           ← REST controllers
    ├── BeneficiaryController.java
    ├── DependentController.java
    ├── BeneficiaryQueryController.java
    └── dto/
        ├── BeneficiaryDto.java           ← record (masked CPF)
        ├── DependentDto.java
        ├── RegisterBeneficiaryRequest.java
        ├── UpdateBeneficiaryRequest.java
        ├── ChangeLifecycleStatusRequest.java
        └── RevealCpfRequest.java
```

**ArchUnit rules** (enforced in `BeneficiaryArchitectureTest`):

- `..beneficiary.application..` may only depend on `..beneficiary..` + `..shared..` + `..socialprogram.api..`
- `..beneficiary.domain..` must not import any Spring annotation except `@DomainEvents` (kept framework-light)
- `..beneficiary.interfaces..` must not import `..*.persistence..` directly
- No class in `..beneficiary..` may import `..payment..`, `..reporting..` packages (only the reverse is allowed)

## 2. Database Schema (PostgreSQL 16, Flyway migrations)

Migration folder: `backend/src/main/resources/db/migration/beneficiary/`

### V001__beneficiary_module_init.sql

```sql
-- Beneficiary aggregate root
CREATE TABLE beneficiary (
    id                       BIGSERIAL PRIMARY KEY,
    cpf                      CHAR(11)        NOT NULL UNIQUE,
    nis                      CHAR(11)        NULL UNIQUE,
    name                     VARCHAR(120)    NOT NULL,
    birth_date               DATE            NOT NULL,
    sex                      CHAR(1)         NOT NULL CHECK (sex IN ('M', 'F')),
    rg                       VARCHAR(20)     NULL,
    -- Embedded address
    addr_logradouro          VARCHAR(120)    NULL,
    addr_numero              VARCHAR(20)     NULL,
    addr_complemento         VARCHAR(60)     NULL,
    addr_bairro              VARCHAR(80)     NULL,
    addr_municipio           VARCHAR(80)     NULL,
    addr_uf                  CHAR(2)         NULL,
    addr_cep                 CHAR(8)         NULL,
    -- Domain state (CL-002 state machine, BONUS-01)
    lifecycle_status         VARCHAR(15)     NOT NULL DEFAULT 'Active',
    age_category             VARCHAR(10)     NOT NULL,           -- derived, refreshed daily
    cpf_validation_status    VARCHAR(20)     NOT NULL DEFAULT 'VALID',  -- ADR-008
    -- Program reference (FK enforced at app level for module boundary, ADR-001)
    program_code             CHAR(4)         NOT NULL,
    -- Lifecycle timestamps
    registered_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    cancelled_at             TIMESTAMPTZ     NULL,
    -- Auditing (Spring Data JPA Auditing)
    created_by               VARCHAR(64)     NOT NULL,
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by               VARCHAR(64)     NOT NULL,
    updated_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version                  BIGINT          NOT NULL DEFAULT 0,  -- optimistic lock
    -- Migration link (ADR-006)
    source_legacy_isn        BIGINT          NULL UNIQUE,
    CONSTRAINT chk_lifecycle CHECK (lifecycle_status IN ('Active','Suspended','Cancelled','Inactive','Disabled')),
    CONSTRAINT chk_age       CHECK (age_category IN ('Minor','Adult','Senior')),
    CONSTRAINT chk_cpf_val   CHECK (cpf_validation_status IN ('VALID','LEGACY_BACKDOOR','TEST'))
);

CREATE INDEX idx_beneficiary_lifecycle ON beneficiary (lifecycle_status) WHERE lifecycle_status != 'Cancelled';
CREATE INDEX idx_beneficiary_program ON beneficiary (program_code);
CREATE INDEX idx_beneficiary_birthdate ON beneficiary (birth_date);
CREATE INDEX idx_beneficiary_legacy_isn ON beneficiary (source_legacy_isn) WHERE source_legacy_isn IS NOT NULL;

-- Dependents as child table (ADR-002)
CREATE TABLE dependent (
    id                       BIGSERIAL PRIMARY KEY,
    beneficiary_id           BIGINT          NOT NULL REFERENCES beneficiary(id) ON DELETE CASCADE,
    cpf                      CHAR(11)        NULL,
    name                     VARCHAR(120)    NOT NULL,
    birth_date               DATE            NOT NULL,
    parentage                VARCHAR(10)     NOT NULL,
    document                 VARCHAR(20)     NULL,
    sex                      CHAR(1)         NULL,
    cpf_validation_status    VARCHAR(20)     NOT NULL DEFAULT 'VALID',
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    source_legacy_isn        BIGINT          NULL,
    CONSTRAINT chk_parentage CHECK (parentage IN ('Spouse','Child','Sibling','Other')),
    CONSTRAINT chk_dep_cpf_val CHECK (cpf_validation_status IN ('VALID','LEGACY_BACKDOOR','TEST'))
);

CREATE INDEX idx_dependent_beneficiary ON dependent (beneficiary_id);
CREATE UNIQUE INDEX uq_dependent_cpf_per_beneficiary
    ON dependent (beneficiary_id, cpf) WHERE cpf IS NOT NULL;  -- FR-013: CPF=null skips uniqueness

COMMENT ON COLUMN beneficiary.cpf_validation_status IS
    'How the CPF was accepted: VALID (Mod-11 strict), LEGACY_BACKDOOR (migrated record), TEST (non-prod test CPF). See ADR-008.';
COMMENT ON COLUMN beneficiary.source_legacy_isn IS
    'Adabas ISN of the source record from FNR 150. NULL for records created in SIFAP 2.0. See ADR-006.';
```

### V002__beneficiary_audit_trigger.sql

```sql
-- Constitution Principle VI: audit_event is immutable
CREATE OR REPLACE FUNCTION fn_audit_event_immutable() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_event rows are immutable (Constitution Principle VI, NFR-COMP-003)';
END;
$$ LANGUAGE plpgsql;

-- Trigger created in audit module (cross-context schema dependency, see ADR-006)
-- Placeholder here to ensure migration order is correct
```

## 3. REST Contract (OpenAPI 3.1)

Base path: `/api/v1/beneficiaries`

| Method | Path | Role | Returns | Errors |
|---|---|---|---|---|
| POST | `/api/v1/beneficiaries` | ADM, OPR | 201 Created | 400 (validation), 409 (conflict), 401, 403 |
| GET | `/api/v1/beneficiaries/{cpf}` | any | 200 OK | 404, 401, 403 |
| GET | `/api/v1/beneficiaries?nis={nis}` | any | 200 OK | 404, 401, 403 |
| PATCH | `/api/v1/beneficiaries/{cpf}` | ADM, OPR | 200 OK | 400, 409, 401, 403 |
| PATCH | `/api/v1/beneficiaries/{cpf}/status` | ADM | 200 OK | 400, 409, 401, 403 |
| DELETE | `/api/v1/beneficiaries/{cpf}` | ADM | 204 No Content | 409 (already cancelled), 401, 403 |
| POST | `/api/v1/beneficiaries/{cpf}/reveal-cpf` | ADM, AUD | 200 OK with full CPF | 400 (missing purpose), 401, 403 |
| POST | `/api/v1/beneficiaries/{cpf}/dependents` | ADM, OPR | 201 Created | 400, 409 (titular inactive, max reached, CPF dup), 401, 403 |
| GET | `/api/v1/beneficiaries/{cpf}/dependents` | any | 200 OK | 404, 401, 403 |
| DELETE | `/api/v1/beneficiaries/{cpf}/dependents/{depId}` | ADM, OPR | 204 No Content | 404, 401, 403 |
| GET | `/api/v1/beneficiaries/{cpf}/payments?limit=N&cursor=X` | any | 200 OK + cursor | 404, 401, 403 |

### CPF in responses

All response payloads return the masked form by default:

```json
{
  "id": 12345,
  "cpf": "XXX.XXX.XXX-09",
  "name": "Maria da Silva",
  "lifecycleStatus": "Active",
  "ageCategory": "Adult",
  ...
}
```

Full CPF appears only in the response of `POST /api/v1/beneficiaries/{cpf}/reveal-cpf`:

```json
{ "cpf": "12345678909", "viewedAt": "2026-05-20T14:32:11Z", "purpose": "audit case #1234" }
```

## 4. Validation Pipeline (Bean Validation + custom)

1. **DTO-level** (`@Valid`): `@NotNull`, `@Size`, `@Pattern` for CPF format, `@Past` for birth date, etc.
2. **Domain-level** (`BrazilianCpfValidator`):
    - Strict Mod-11 in production
    - Mod-11 + backdoor list in non-prod (FR-004)
    - All-equal-digits rejection unless test backdoor flag is on
3. **Aggregate-level** (`Beneficiary` constructor):
    - Name has ≥ 2 whitespace-separated tokens (FR-006)
    - UF in the 27-entry whitelist (FR-007)
    - Birth date in the past (FR-001 acceptance criterion 2)
4. **Cross-aggregate** (`BeneficiaryService`):
    - Program code exists and is active (via `SocialProgramQueryPort`)
    - NIS not in use by another active beneficiary
    - CPF not already in `Active` status

## 5. Event Publication

Spring Application Events (in-process, ADR-001):

```java
@DomainEvents
Collection<Object> domainEvents() {
    return List.of(new BeneficiaryRegistered(this.id, this.cpf, this.registeredAt));
}
```

The `ReportingAndAudit` context subscribes via `@EventListener` and persists `audit_event` rows. No transport (Service Bus) needed inside the monolith.

## 6. CPF Backdoor Enforcement (ADR-008)

```java
@Configuration
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "prod")
class CpfBackdoorBootGuard {
    @Value("${sifap.cpf.allowTestCpfs:false}") boolean allowTestCpfs;

    @PostConstruct
    void enforce() {
        if (allowTestCpfs) {
            throw new IllegalStateException(
                "FATAL: sifap.cpf.allowTestCpfs MUST be false in production. " +
                "See ADR-008. Refusing to start.");
        }
    }
}
```

## 7. Daily Age-Category Job (CL-003)

```java
@Component
class AgeCategoryRecomputeJob {
    @Scheduled(cron = "${sifap.jobs.ageCategory.cron:0 0 2 * * *}", zone = "America/Sao_Paulo")
    void recompute() {
        // single SQL UPDATE; emit events for changed rows
    }
}
```

## 8. Test Strategy (Constitution Principle III)

| Layer | Tool | Coverage target | Examples |
|---|---|---|---|
| Domain unit | JUnit 5 + AssertJ | 100% on `Cpf`, `BrazilianCpfValidator`, `BrazilianCpfMasker`, enum transitions | All Mod-11 boundary cases; backdoor accept/reject per env |
| Application service | JUnit 5 + Mockito | ≥ 90% | Each service method's happy path + 2-3 error paths |
| Repository (integration) | JUnit 5 + Testcontainers (PostgreSQL 16) | smoke tests for FK + unique constraints | dependent CPF uniqueness when not null |
| API (integration) | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + RestAssured | All endpoints, all roles | 401 for missing token; 403 for wrong role; 201 for happy path |
| Architecture | ArchUnit | gating | rules in §1 |
| Boot guard | `@SpringBootTest(properties = {"spring.profiles.active=prod","sifap.cpf.allowTestCpfs=true"})` | this test MUST fail to boot | ADR-008 |
| Equivalence (legacy ↔ new) | JUnit 5 fixture | 10,000 randomized CPFs | accept/reject decision matches the Java port of `VALBENEF.NSN` |
| Security (OWASP) | OWASP ZAP in CI | 0 high/critical | run before merge |
| Log filter | unit + integration | CPF never appears as 11 raw digits in logs | grep-based assertion |

## 9. Observability

Metrics (Micrometer, NFR-OBS-003):

- `sifap.beneficiary.registered.count` (counter)
- `sifap.beneficiary.cpf.rejected.count` (counter by reason: mod11, duplicate, immutable)
- `sifap.beneficiary.reveal.count` (counter)
- `sifap.beneficiary.cpf_backdoor.accepted.count` (counter — must be 0 in prod)
- `sifap.beneficiary.api.latency` (timer per endpoint)

Alerts:

- `sifap.beneficiary.cpf_backdoor.accepted.count > 0` AND `environment = prod` → **CRITICAL** (defense in depth on ADR-008)
- API P95 > 300ms over 5 min window → WARN (NFR-PERF-001)

## 10. Performance Plan (NFR-PERF)

| Endpoint | P95 budget | Index strategy |
|---|---|---|
| GET /api/v1/beneficiaries/{cpf} | 300 ms | unique index on `cpf` |
| GET /api/v1/beneficiaries?nis={nis} | 300 ms | unique index on `nis` |
| POST /api/v1/beneficiaries | 500 ms | constraint checks via index |
| GET /api/v1/beneficiaries/{cpf}/payments | 200 ms | `(cpf, competence DESC)` index on `payment` (created in PaymentProcessing migration) |

Connection pool: HikariCP default 10 connections; load tests confirm sufficiency at 100 RPS.

## 11. Security Plan (NFR-SEC, ADR-007, ADR-008)

| Concern | Mitigation |
|---|---|
| CPF in logs | `MaskedCpfLogFilter` + grep CI gate |
| Full CPF exposure | reveal endpoint requires `ADM` or `AUD` + non-empty `purpose` + audit event |
| Production backdoor activation | `CpfBackdoorBootGuard` prevents boot if flag is on |
| SQL injection | Spring Data JPA + JPQL (no native queries with string concatenation) |
| Mass assignment | DTO objects separate from entities; no `@RequestBody Beneficiary` ever |
| RBAC | `@PreAuthorize("hasAnyRole('ADM','OPR')")` on state-changing endpoints |

## 12. Dependencies on Other Bounded Contexts

- **SocialProgramRegistry**: read-only port `SocialProgramQueryPort.findActiveByCode(programCode)` for FR-001 program validation. Implementation in `socialprogram.api` package.
- **ReportingAndAudit**: consumes domain events; this context does not depend on it (one-way relationship).

## 13. Migration Considerations (ADR-006)

- Records loaded from Adabas snapshot carry `source_legacy_isn` and may have `cpf_validation_status = LEGACY_BACKDOOR`.
- The service layer rejects mutations on `LEGACY_BACKDOOR` records that would change the CPF to another invalid value (FR-025).
- A separate `BeneficiaryLegacyMigrationService` (not part of this spec) handles the ETL; this spec assumes data is present.

## 14. Risks and Open Items

| Risk | Mitigation |
|---|---|
| `[PENDING-SENARC]` on `max_dependents` default | Configurable per program; safe to ship with 5 |
| OAuth/JWT integration not yet wired | Mock authorization in tests; require Par 5 to provision Entra ID app before deployment |
| Existing legacy records with backdoor CPFs land in production DB | `cpf_validation_status = LEGACY_BACKDOOR` makes them readable but immutable until corrected |
| Cross-context coupling via direct SQL | ArchUnit rules in §1 catch violations in CI |

## 15. Out of Scope (deferred to other features/specs)

- Payment calculation (PaymentProcessing context)
- Eligibility validation across programs (SocialProgramRegistry context)
- Notification on lifecycle change (no Notification context yet, see `bounded-contexts.md` rejected hypothesis E)
- Bulk CSV import
- Public self-service registration

## 16. Constitution Compliance Checklist

- [x] Principle I (Legacy Traceability): every FR in spec.md has `source_legacy`
- [x] Principle II (Modular Monolith): package layout in §1 + ArchUnit rules
- [x] Principle III (Test-First): test strategy in §8, written alongside code
- [x] Principle IV (Stack Discipline): Java 21 + Spring Boot 3.3 + PostgreSQL 16, no extra deps
- [x] Principle V (Single Source of Truth): plan references baseline files, doesn't duplicate
- [x] Principle VI (Security & Compliance): §11 covers all 5 inviolable rules
- [x] Principle VII (Observability): §9 covers logs/metrics/traces/alerts

---

## References

- Spec: [`spec.md`](spec.md)
- Constitution: `.specify/memory/constitution.md`
- Tech stack: [`../../02-spec-moderna/baseline/techstack.md`](../../02-spec-moderna/baseline/techstack.md)
- Bounded contexts: [`../../02-spec-moderna/baseline/bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md)
- ADRs: [001](../../02-spec-moderna/baseline/ADR-001-modular-monolith.md), [002](../../02-spec-moderna/baseline/ADR-002-pe-groups-mapping.md), [006](../../02-spec-moderna/baseline/ADR-006-data-migration-strategy.md), [007](../../02-spec-moderna/baseline/ADR-007-pii-masking-policy.md), [008](../../02-spec-moderna/baseline/ADR-008-cpf-test-backdoors.md)
