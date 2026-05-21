# Implementation Plan: Social Program Registry

**Feature**: `003-social-program-registry` · **Spec**: [`spec.md`](spec.md)
**Status**: Draft · 2026-05-20

## 1. Module Layout

```text
com.sifap.socialprogram/
├── domain/
│   ├── SocialProgram.java                ← aggregate root (JPA entity)
│   ├── RegionalFactor.java               ← child entity
│   ├── ProgramType.java                  ← enum A/P/T
│   ├── ProgramStatus.java                ← Active/Suspended/Retired with allowed transitions
│   └── events/
│       ├── SocialProgramCreated.java
│       ├── ProgramAdjustmentFactorChanged.java
│       ├── RegionalFactorsReplaced.java
│       └── ProgramStatusChanged.java
├── application/
│   └── SocialProgramService.java         ← transactional boundary, cache invalidation
├── infrastructure/
│   ├── persistence/
│   │   ├── SocialProgramRepository.java
│   │   └── RegionalFactorRepository.java
│   └── cache/
│       └── CachedSocialProgramAdapter.java  ← Caffeine cache + write-through invalidation
├── interfaces/
│   ├── SocialProgramController.java
│   ├── RegionalFactorController.java
│   ├── SocialProgramExceptionHandler.java
│   └── dto/...
└── api/                                  ← PUBLISHED PORT (consumed by 001)
    └── SocialProgramQueryPort.java       ← copy of the contract defined in feature 001
```

The class at `..api.SocialProgramQueryPort` is the **only** symbol other contexts may import (ArchUnit-enforced).

## 2. Flyway V001 — `db/migration/socialprogram/V001__social_program_init.sql`

```sql
CREATE TABLE social_program (
    id                 BIGSERIAL PRIMARY KEY,
    code               CHAR(4)       NOT NULL UNIQUE,
    type               CHAR(1)       NOT NULL CHECK (type IN ('A','P','T')),
    base_amount        NUMERIC(15,2) NOT NULL CHECK (base_amount >= 0),
    adjustment_factor  NUMERIC(6,4)  NOT NULL CHECK (adjustment_factor BETWEEN -1 AND 1),
    status             VARCHAR(10)   NOT NULL DEFAULT 'Active'
                       CHECK (status IN ('Active','Suspended','Retired')),
    effective_from     DATE          NOT NULL,
    effective_until    DATE          NULL,
    created_by         VARCHAR(64)   NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by         VARCHAR(64)   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version            BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_program_status ON social_program (status) WHERE status != 'Retired';

CREATE TABLE regional_factor (
    id           BIGSERIAL PRIMARY KEY,
    program_id   BIGINT      NOT NULL REFERENCES social_program(id) ON DELETE CASCADE,
    uf_code      CHAR(2)     NOT NULL,
    factor       NUMERIC(5,3) NOT NULL CHECK (factor BETWEEN 0.5 AND 2.0),
    CONSTRAINT uq_program_uf UNIQUE (program_id, uf_code),
    CONSTRAINT chk_no_region_99 CHECK (uf_code !~ '^[0-9]')  -- UFs are letters; 99 forbidden (FR-012)
);

CREATE INDEX idx_regional_factor_program ON regional_factor (program_id);
```

## 3. REST Contract

| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/api/v1/social-programs` | ADM | Create |
| GET | `/api/v1/social-programs/{code}` | any auth | Read |
| GET | `/api/v1/social-programs?status=&type=` | any auth | List |
| PATCH | `/api/v1/social-programs/{code}` | ADM | Update fields (incl. `adjustmentFactor`) |
| PATCH | `/api/v1/social-programs/{code}/status` | ADM | Status transition |
| PUT | `/api/v1/social-programs/{code}/regional-factors` | ADM | Replace 27-UF table atomically |
| GET | `/api/v1/social-programs/{code}/regional-factors/{uf}` | any auth | Read single factor |

## 4. Caching Strategy

Read path is hot during a payment cycle (one call per beneficiary × per UF lookup). Adapter wraps the repository with Caffeine:

- Cache key: `(programCode)` for `findActiveByCode`, `(programCode, ufCode)` for `regionalFactor`.
- Eviction: write-through on every PATCH/PUT by the service.
- TTL: 30 minutes (defensive, even if eviction is missed).
- Size cap: 10,000 entries (well above the ~30 programs × 27 UFs ≈ 810 entries the system holds).

## 5. Test Strategy

| Layer | Coverage |
|---|---|
| Domain unit | enum transitions, validation rules, 100% on `SocialProgram.changeStatus()` |
| Service | happy + 409 (duplicate) + invalid type change + concurrent update |
| Repository | Testcontainers PostgreSQL, FK + UNIQUE constraints |
| API | RestAssured, RBAC matrix, 400/401/403/404/409 + RFC 7807 |
| Cache | write-through invalidation, read-after-write consistency |
| ArchUnit | only `api/` package may be referenced by other contexts |
| Contract | port `SocialProgramQueryPort` matches the version expected by feature 001 |

## 6. Integration with Feature 001

The `api/SocialProgramQueryPort.java` interface in this module **must match byte-for-byte** the version `001-payment-cycle-generation` declares. A contract test (`SocialProgramPortContractTest`) verifies signatures match between the two branches when both merge into `develop`.

## 7. Constitution Compliance

- I (Legacy Traceability): every FR cites `CADPROG.NSN` or `[GREENFIELD]`
- II (Modular Monolith): published port pattern via `api/`
- III (Test-First): tests at every layer
- IV (Stack Discipline): Java 21 + Spring Boot 3.3 + Flyway + Caffeine — no new deps
- V (Single Source of Truth): K-factor magic constant stays in `KFactor` (001); this module owns the input only
- VI (Security): RBAC, audit, 10-year retention
- VII (Observability): cache hit/miss metrics, `sifap.program.*` counters
