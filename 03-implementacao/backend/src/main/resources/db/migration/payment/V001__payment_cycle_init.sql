-- Flyway migration for PaymentProcessing module (feature 001).
-- See specs/001-payment-cycle-generation/plan.md §2 for design rationale.

CREATE TABLE payment_cycle (
    id                       BIGSERIAL PRIMARY KEY,
    competence               CHAR(7)         NOT NULL,
    program_code             CHAR(4)         NOT NULL,
    status                   VARCHAR(15)     NOT NULL DEFAULT 'Pending',
    triggered_by             VARCHAR(64)     NOT NULL,
    correlation_id           UUID            NOT NULL,
    started_at               TIMESTAMPTZ     NULL,
    completed_at             TIMESTAMPTZ     NULL,
    generated_count          BIGINT          NOT NULL DEFAULT 0,
    skipped_count            BIGINT          NOT NULL DEFAULT 0,
    error_count              BIGINT          NOT NULL DEFAULT 0,
    region99_bypass_count    BIGINT          NOT NULL DEFAULT 0,
    total_gross_amount       NUMERIC(18,2)   NOT NULL DEFAULT 0,
    snapshot_at              TIMESTAMPTZ     NULL,
    force_backfill           BOOLEAN         NOT NULL DEFAULT false,
    backfill_reason          TEXT            NULL,
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT now(),
    version                  BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_status     CHECK (status IN ('Pending','Running','Completed','Aborted','Stale')),
    CONSTRAINT chk_competence CHECK (competence ~ '^[0-9]{4}-(0[1-9]|1[0-2])$'),
    CONSTRAINT uq_cycle       UNIQUE (competence, program_code)
);

CREATE INDEX idx_payment_cycle_status ON payment_cycle (status)
    WHERE status IN ('Pending','Running','Stale');

CREATE TABLE payment (
    id                       BIGSERIAL,
    cycle_id                 BIGINT          NOT NULL REFERENCES payment_cycle(id),
    beneficiary_id           BIGINT          NOT NULL,
    cpf                      CHAR(11)        NOT NULL,
    competence               CHAR(7)         NOT NULL,
    program_code             CHAR(4)         NOT NULL,
    payment_type             VARCHAR(10)     NOT NULL,
    gross_amount             NUMERIC(15,2)   NOT NULL,
    christmas_allowance      NUMERIC(15,2)   NOT NULL DEFAULT 0,
    status                   VARCHAR(10)     NOT NULL DEFAULT 'Pending',
    factors                  JSONB           NOT NULL,
    region_code              SMALLINT        NOT NULL,
    region_bypass            BOOLEAN         NOT NULL DEFAULT false,
    generated_at             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    voided_at                TIMESTAMPTZ     NULL,
    PRIMARY KEY (id, competence),
    CONSTRAINT chk_pay_type   CHECK (payment_type IN ('MONTHLY','THIRTEENTH')),
    CONSTRAINT chk_pay_status CHECK (status IN ('Pending','Released','Paid','Voided'))
) PARTITION BY RANGE (competence);

CREATE TABLE payment_2026 PARTITION OF payment FOR VALUES FROM ('2026-01') TO ('2027-01');
CREATE TABLE payment_2027 PARTITION OF payment FOR VALUES FROM ('2027-01') TO ('2028-01');

CREATE UNIQUE INDEX uq_payment_per_competence ON payment (cpf, competence, program_code);
CREATE INDEX idx_payment_by_cpf   ON payment (cpf, competence DESC);
CREATE INDEX idx_payment_by_cycle ON payment (cycle_id);

CREATE TABLE cycle_failure (
    id              BIGSERIAL PRIMARY KEY,
    cycle_id        BIGINT       NOT NULL REFERENCES payment_cycle(id),
    beneficiary_id  BIGINT       NOT NULL,
    reason          VARCHAR(80)  NOT NULL,
    stack_trace     TEXT         NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_failure_by_cycle ON cycle_failure (cycle_id);

CREATE TABLE cycle_skip (
    id              BIGSERIAL PRIMARY KEY,
    cycle_id        BIGINT       NOT NULL REFERENCES payment_cycle(id),
    beneficiary_id  BIGINT       NOT NULL,
    reason          VARCHAR(40)  NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_skip_reason
        CHECK (reason IN ('NotActive','Ineligible','MissingRegionFactor','ProgramMismatch','LegacyBackdoorBlocked'))
);
CREATE INDEX idx_skip_by_cycle ON cycle_skip (cycle_id);

COMMENT ON COLUMN payment.region_bypass IS
    'TRUE when region_code = 99 (BR-024, MYS-008). Auditable forever.';
COMMENT ON COLUMN payment_cycle.snapshot_at IS
    'All cycle decisions use beneficiary/program state as of this timestamp.';
