-- Flyway migration for Beneficiary module (feature 002).
-- See specs/002-beneficiary-registration/plan.md §2 for design rationale.

CREATE TABLE beneficiary (
    id                       BIGSERIAL PRIMARY KEY,
    cpf                      CHAR(11)        NOT NULL UNIQUE,
    nis                      CHAR(11)        NULL UNIQUE,
    name                     VARCHAR(120)    NOT NULL,
    birth_date               DATE            NOT NULL,
    sex                      CHAR(1)         NOT NULL CHECK (sex IN ('M', 'F')),
    rg                       VARCHAR(20)     NULL,
    addr_logradouro          VARCHAR(120)    NULL,
    addr_numero              VARCHAR(20)     NULL,
    addr_complemento         VARCHAR(60)     NULL,
    addr_bairro              VARCHAR(80)     NULL,
    addr_municipio           VARCHAR(80)     NULL,
    addr_uf                  CHAR(2)         NULL,
    addr_cep                 CHAR(8)         NULL,
    lifecycle_status         VARCHAR(15)     NOT NULL DEFAULT 'Active',
    age_category             VARCHAR(10)     NOT NULL,
    cpf_validation_status    VARCHAR(20)     NOT NULL DEFAULT 'VALID',
    program_code             CHAR(4)         NOT NULL,
    registered_at            TIMESTAMPTZ     NOT NULL DEFAULT now(),
    cancelled_at             TIMESTAMPTZ     NULL,
    created_by               VARCHAR(64)     NOT NULL,
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_by               VARCHAR(64)     NOT NULL,
    updated_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version                  BIGINT          NOT NULL DEFAULT 0,
    source_legacy_isn        BIGINT          NULL UNIQUE,
    CONSTRAINT chk_lifecycle CHECK (lifecycle_status IN ('Active','Suspended','Cancelled','Inactive','Disabled')),
    CONSTRAINT chk_age       CHECK (age_category IN ('Minor','Adult','Senior')),
    CONSTRAINT chk_cpf_val   CHECK (cpf_validation_status IN ('VALID','LEGACY_BACKDOOR','TEST'))
);

CREATE INDEX idx_beneficiary_lifecycle ON beneficiary (lifecycle_status)
    WHERE lifecycle_status != 'Cancelled';
CREATE INDEX idx_beneficiary_program    ON beneficiary (program_code);
CREATE INDEX idx_beneficiary_birthdate  ON beneficiary (birth_date);
CREATE INDEX idx_beneficiary_legacy_isn ON beneficiary (source_legacy_isn) WHERE source_legacy_isn IS NOT NULL;

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
    CONSTRAINT chk_parentage    CHECK (parentage IN ('Spouse','Child','Sibling','Other')),
    CONSTRAINT chk_dep_cpf_val  CHECK (cpf_validation_status IN ('VALID','LEGACY_BACKDOOR','TEST'))
);

CREATE INDEX idx_dependent_beneficiary ON dependent (beneficiary_id);
CREATE UNIQUE INDEX uq_dependent_cpf_per_beneficiary
    ON dependent (beneficiary_id, cpf) WHERE cpf IS NOT NULL;

COMMENT ON COLUMN beneficiary.cpf_validation_status IS
    'VALID (Mod-11), LEGACY_BACKDOOR (migrated row), TEST (non-prod). See ADR-008.';
COMMENT ON COLUMN beneficiary.source_legacy_isn IS
    'Adabas ISN from FNR 150. NULL for SIFAP 2.0-native rows. See ADR-006.';
