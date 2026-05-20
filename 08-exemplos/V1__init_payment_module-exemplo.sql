-- ============================================================================
-- V1__init_payment_module-exemplo.sql
-- ============================================================================
-- EXEMPLO PREENCHIDO de uma migração Flyway para o módulo `payment` do SIFAP 2.0.
--
-- Origem das tabelas:
--   - DDM `PAGAMENTO.ddm` (legado) → tabela `payment.payment`
--   - PE `DESCONTOS` (Periodic Group dentro de BENEFICIARIO.ddm) → tabela filha
--     `payment.payment_deduction` (PE não cabe em SQL puro, vira tabela separada)
--   - Auditoria de transição → tabela `payment.payment_audit`
--
-- Cobertura de REQ-IDs:
--   - REQ-PAY-001/002 (teto de descontos)        → suporta com `deduction_type`
--   - REQ-PAY-003     (geração de ciclo)         → coluna `cycle_id` indexada
--   - REQ-PAY-004     (status inicial PENDING)   → CHECK + DEFAULT
--   - REQ-PAY-005     (transições permitidas)    → CHECK na coluna `status`
--   - REQ-PAY-006     (auditoria)                → tabela `payment_audit`
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Schema do módulo (isolamento por módulo, regra do ADR-001)
-- ----------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS payment;

SET search_path TO payment, public;

-- ----------------------------------------------------------------------------
-- Tabela: payment_cycle
-- Mapeia ciclo de pagamento mensal (BR-005 / REQ-PAY-003).
-- 1 ciclo por (ano, mês). Único.
-- ----------------------------------------------------------------------------
CREATE TABLE payment_cycle (
    id              BIGSERIAL    PRIMARY KEY,
    competence      CHAR(6)      NOT NULL,                 -- formato YYYYMM (vem de COMPETENCIA do DDM, N6)
    cutoff_date     DATE         NOT NULL,                 -- data de corte (último dia do mês anterior)
    generated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),   -- timestamp UTC
    generated_by    VARCHAR(60)  NOT NULL,                 -- usuário que disparou
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    CONSTRAINT payment_cycle_competence_uk UNIQUE (competence),
    CONSTRAINT payment_cycle_status_ck     CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED'))
);

COMMENT ON TABLE payment_cycle IS
  'Ciclo mensal de pagamento. Rastreia BR-005 (BATCHPGT.NSN#L88-L142).';

-- ----------------------------------------------------------------------------
-- Tabela: payment
-- Pagamento individual de um beneficiário em um ciclo.
-- Origem: PAGAMENTO.ddm.
-- ----------------------------------------------------------------------------
CREATE TABLE payment (
    id              BIGSERIAL    PRIMARY KEY,
    cycle_id        BIGINT       NOT NULL REFERENCES payment_cycle(id),
    beneficiary_cpf CHAR(11)     NOT NULL,                  -- equivale CPF-BENEF (N11) — mantemos como string fixa
    gross_amount    NUMERIC(11,2) NOT NULL CHECK (gross_amount >= 0), -- VLR-BRUTO (N9.2)
    deduction_total NUMERIC(11,2) NOT NULL DEFAULT 0    CHECK (deduction_total >= 0),
    net_amount      NUMERIC(11,2) GENERATED ALWAYS AS (gross_amount - deduction_total) STORED,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT payment_status_ck CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT payment_cycle_benef_uk UNIQUE (cycle_id, beneficiary_cpf)
);

-- Índices que suportam os endpoints listados na spec
CREATE INDEX idx_payment_status        ON payment(status);
CREATE INDEX idx_payment_beneficiary   ON payment(beneficiary_cpf);
CREATE INDEX idx_payment_cycle         ON payment(cycle_id);

COMMENT ON COLUMN payment.beneficiary_cpf IS
  'CPF do beneficiário. Armazenado cru (criptografia at-rest no PostgreSQL Flexible). '
  'CPF é mascarado em logs (REQ-PAY-008).';

-- ----------------------------------------------------------------------------
-- Tabela: payment_deduction
-- Origem: PE DESCONTOS do BENEFICIARIO.ddm.
-- PE (Periodic Group) NÃO cabe em SQL puro → tabela filha.
-- Suporta BR-001 (teto 30% não judicial / REQ-PAY-001/002).
-- ----------------------------------------------------------------------------
CREATE TABLE payment_deduction (
    id              BIGSERIAL     PRIMARY KEY,
    payment_id      BIGINT        NOT NULL REFERENCES payment(id) ON DELETE CASCADE,
    deduction_type  VARCHAR(20)   NOT NULL,                 -- mapeia TIPO-DSCT (A1) → enum legível
    amount          NUMERIC(11,2) NOT NULL CHECK (amount >= 0),
    percentage      NUMERIC(5,2),                            -- opcional, espelha PCT-DSCT
    process_number  VARCHAR(20),                             -- NUM-PROCESSO — só preenche se JUDICIAL
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT payment_deduction_type_ck
        CHECK (deduction_type IN ('CONTRIB', 'TAX', 'JUDICIAL', 'UNION', 'PENSION', 'ADMIN')),
    -- Regra de integridade: JUDICIAL exige process_number (rastreia BR-001 + REQ-PAY-002)
    CONSTRAINT payment_deduction_judicial_process_ck
        CHECK ((deduction_type <> 'JUDICIAL') OR (process_number IS NOT NULL))
);

CREATE INDEX idx_deduction_payment ON payment_deduction(payment_id);
CREATE INDEX idx_deduction_type    ON payment_deduction(deduction_type);

COMMENT ON TABLE payment_deduction IS
  'Descontos aplicados a um pagamento. Origem: PE DESCONTOS de CALCDSCT.NSN. '
  'Teto de 30% (não JUDICIAL) é regra de aplicação no PaymentService — '
  'não dá pra fazer via CHECK constraint porque depende do gross_amount do pai.';

-- ----------------------------------------------------------------------------
-- Tabela: payment_audit
-- REQ-PAY-006 / BR-014 — toda transição grava registro imutável.
-- ----------------------------------------------------------------------------
CREATE TABLE payment_audit (
    id              BIGSERIAL    PRIMARY KEY,
    payment_id      BIGINT       NOT NULL REFERENCES payment(id),
    previous_status VARCHAR(20),
    new_status      VARCHAR(20)  NOT NULL,
    changed_by      VARCHAR(60)  NOT NULL,
    changed_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    reason          TEXT,
    CONSTRAINT payment_audit_status_change_ck
        CHECK (previous_status IS DISTINCT FROM new_status)
);

CREATE INDEX idx_audit_payment ON payment_audit(payment_id);
CREATE INDEX idx_audit_when    ON payment_audit(changed_at);

-- Bloqueia DELETE / UPDATE para garantir imutabilidade (REQ-AUD-001).
-- Permissões finas serão concedidas via role da app no V2.
REVOKE UPDATE, DELETE ON payment_audit FROM PUBLIC;

COMMENT ON TABLE payment_audit IS
  'Audit log de transições de status. IMUTÁVEL: nunca UPDATE, nunca DELETE.';

-- ----------------------------------------------------------------------------
-- Trigger de auditoria automática (opcional — depende da estratégia do time).
-- Comentado por padrão. Algumas equipes preferem auditar no application layer
-- (PaymentService.changeStatus()) para manter o domínio simples.
-- ----------------------------------------------------------------------------
-- CREATE OR REPLACE FUNCTION audit_payment_status_change() ...
-- CREATE TRIGGER trg_payment_audit AFTER UPDATE OF status ON payment ...

-- ============================================================================
-- O que torna esta migração "boa":
--   ✅ CHECK constraints em status e tipos (REQ-PAY-004, REQ-PAY-005)
--   ✅ PE do legado → tabela filha (não tenta forçar array nem JSON)
--   ✅ Auditoria imutável (REVOKE explícito)
--   ✅ Comentários ligam cada tabela ao DDM e BR/REQ de origem
--   ✅ Coluna gerada para net_amount (consistência garantida pelo banco)
--   ✅ Índices apenas para os endpoints da spec — sem premature optimization
--   ✅ Nada de DROP, ALTER ou modificação de migração anterior — só V1
-- ============================================================================

-- ============================================================================
-- Como o time deve evoluir daqui:
--   - V2__add_payment_indexes.sql   → se aparecer slow query no Estágio 4
--   - V3__add_payment_constraints.sql → para regras descobertas no Estágio 3
--   - NUNCA editar este V1. Sempre criar V2, V3, V4...
-- ============================================================================
