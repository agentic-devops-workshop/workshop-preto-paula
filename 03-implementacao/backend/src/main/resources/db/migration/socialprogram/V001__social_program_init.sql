-- Flyway V001 for SocialProgramRegistry (feature 003).
-- See specs/003-social-program-registry/plan.md §2.

CREATE TABLE social_program (
    id                BIGSERIAL PRIMARY KEY,
    code              CHAR(4)       NOT NULL UNIQUE,
    type              CHAR(1)       NOT NULL CHECK (type IN ('A','P','T')),
    base_amount       NUMERIC(15,2) NOT NULL CHECK (base_amount >= 0),
    adjustment_factor NUMERIC(6,4)  NOT NULL CHECK (adjustment_factor BETWEEN -1 AND 1),
    status            VARCHAR(10)   NOT NULL DEFAULT 'Active'
                      CHECK (status IN ('Active','Suspended','Retired')),
    effective_from    DATE          NOT NULL,
    effective_until   DATE          NULL,
    created_by        VARCHAR(64)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_by        VARCHAR(64)   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
    version           BIGINT        NOT NULL DEFAULT 0
);

CREATE INDEX idx_program_status ON social_program (status) WHERE status != 'Retired';

CREATE TABLE regional_factor (
    id          BIGSERIAL PRIMARY KEY,
    program_id  BIGINT       NOT NULL REFERENCES social_program(id) ON DELETE CASCADE,
    uf_code     CHAR(2)      NOT NULL,
    factor      NUMERIC(5,3) NOT NULL CHECK (factor BETWEEN 0.5 AND 2.0),
    CONSTRAINT uq_program_uf UNIQUE (program_id, uf_code),
    -- FR-012: region 99 is a beneficiary bypass marker, not a real UF (MYS-008).
    CONSTRAINT chk_no_region_99 CHECK (uf_code !~ '^[0-9]')
);

CREATE INDEX idx_regional_factor_program ON regional_factor (program_id);

COMMENT ON COLUMN social_program.adjustment_factor IS
    'Input to K-factor in PaymentCycle: K = 1 + adjustment_factor × 0.347215 (BR-010, MYS-003).';
