<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-006: Data Migration Strategy — Strangler Fig with Read-Through Cutover

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY Critical](https://img.shields.io/badge/SEVERITY-Critical-F25022?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture
- Reviewed by: Par 4 · Quality (DBA) — owns migration pipeline
- Reviewed by: Par 5 · Operations (DevOps) — owns infrastructure
- Reviewed by: Par 1 · Vision (Product Owner) — cutover window approval

## Technical Story

SIFAP legacy stores:

- **~4.2 million** active beneficiaries (`BENEFICIARIO.ddm`, file 150)
- **~45 active** social programs (`PROGRAMA-SOCIAL.ddm`, file 151)
- **~180 million** historical payment records (`PAGAMENTO.ddm`, file 152), growing ~3.8M/month
- **~25 million** audit events (`AUDITORIA.ddm`, file 153), 10-year legal retention

The system is **always on** — monthly payments cannot be delayed. We have no maintenance window long enough to:

- Stop writes, snapshot, transform, load, validate, switch over — all in one outage

Additionally, the legacy must continue running until the new system is proven equivalent for at least 3 payment cycles.

## Context and Problem Statement

We need a migration strategy that:

1. Brings 180M payments + 4.2M beneficiaries into PostgreSQL
2. Maintains the legacy running until cutover is validated
3. Allows comparing legacy vs new output payment-by-payment
4. Supports rollback at any point (back to legacy)
5. Fits a workshop scope (proof of concept) while documenting a production-ready approach
6. Preserves source_legacy traceability (every migrated record must be linkable to its Adabas ISN)

The migration is a **system risk** more than a technical risk. The biggest danger is **silent data corruption** — a beneficiary whose payment is miscalculated for 6 months before anyone notices.

## Decision Drivers

- **D1 — Zero downtime tolerance**: legacy is critical infrastructure; cannot stop for migration
- **D2 — Reversibility**: must be able to fall back to legacy at any point during transition
- **D3 — Validation depth**: must compare legacy ↔ new output per beneficiary, per payment, per month
- **D4 — Workshop reality**: 8 hours total, so the *implementation* must be a documented plan + proof-of-concept ETL, not a real 180M-row load
- **D5 — Auditability**: every migrated record needs a `source_legacy_isn` so any operator question traces back
- **D6 — Long retention**: 10 years of audit data must survive intact
- **D7 — Bounded contexts**: migration can happen per context, not all-at-once

## Considered Options

1. **Big Bang ETL with cutover** — stop legacy, ETL all data, start new, no return
2. **Strangler Fig with CDC (Change Data Capture)** — new system reads + writes to its own DB; CDC keeps legacy → new sync until cutover per context
3. **Permanent coexistence** — both systems run forever, federated via a routing layer
4. **Shadow read** — new system reads from PostgreSQL for new payments; historical reads proxy to legacy via gateway

## Decision

**Chosen option: 2 — Strangler Fig with CDC (per bounded context cutover).**

Migration runs in **6 phases**, per bounded context, in dependency order:

### Phase 0 — Snapshot baseline (offline)

- One-time ETL via Adabas `ADAULD` (export utility) → CSV → PostgreSQL COPY
- Loads:
  - `social_program` (45 rows, trivial)
  - `beneficiary` (4.2M rows + dependents + discounts)
  - `payment` (180M rows, partitioned by hash of CPF, 16 partitions)
  - `audit_event` (25M rows)
- Every row gets `source_legacy_isn BIGINT NOT NULL` column → links to Adabas record
- Validation: row counts match, checksums of key fields match (CPF list hash, total payment amount per competence)

### Phase 1 — CDC bridge (real-time)

- Custom CDC reader (Java) polls Adabas `*LOG` (audit file) every 60s
- Detected changes → enqueued in Azure Service Bus → consumed by SIFAP 2.0 `IngestService` → applied to PostgreSQL
- Conflict resolution: legacy wins (legacy is source of truth during transition)
- Direction: **legacy → new only** (no write-back yet)

### Phase 2 — Shadow mode (read-only validation)

- SIFAP 2.0 calculates payments **in parallel** with legacy (legacy still authoritative)
- Output compared: if any beneficiary's payment differs by > R$ 0.01, alert + investigate
- Duration: at least 3 monthly cycles
- Success criterion: > 99.99% match across all 3 cycles

### Phase 3 — Read cutover per bounded context

- Inbound queries (e.g., `/api/v1/beneficiaries/{cpf}`) hit SIFAP 2.0 PostgreSQL
- Writes still go to legacy → CDC propagates back to PostgreSQL
- Bounded context order:
  1. `ReportingAndAudit` (read-only, lowest risk)
  2. `BeneficiaryManagement` (queries don't trigger payments)
  3. `SocialProgramRegistry` (low write volume)
  4. `PaymentProcessing` (last and most cautious)

### Phase 4 — Write cutover per bounded context

- SIFAP 2.0 owns writes for a context (e.g., `BeneficiaryManagement` first)
- CDC reverses direction: **new → legacy** (legacy becomes secondary, kept for 90 days as safety net)
- Each context cutover triggered by explicit GO from operator after manual validation

### Phase 5 — Decommission legacy (per context)

- After 3 months stable with new system as primary AND legacy untouched, legacy DDM is frozen (read-only)
- After 12 months untouched, legacy DDM is archived to cold storage
- `source_legacy_isn` columns kept indefinitely (audit traceability)

### Workshop scope

The workshop builds:

- ✅ Schema with `source_legacy_isn` columns
- ✅ Proof-of-concept ETL script that loads a 10K-row sample
- ✅ Comparison test framework (legacy snapshot vs new calc)
- ✅ Documented runbook for Phases 1-5 (not executed in 8h)
- ⏭ Real CDC implementation, Service Bus setup, 3-month shadow mode — **out of workshop scope**

## Pros and Cons of the Options

### Option 1 — Big Bang ETL with cutover

- ✅ Simplest mental model
- ✅ Single cutover date — clean before/after
- ❌ **No rollback after cutover** — if a bug is found on day 3, legacy is already cold; restoring it is days of work
- ❌ **Requires maintenance window** SIFAP cannot afford (payment ops are weekly minimum)
- ❌ **No parallel validation possible** — bugs only surface after cutover when blast radius is 100%
- ❌ Rejected as **unacceptable risk** for financial system at this scale

### Option 2 — Strangler Fig with CDC (chosen)

- ✅ Zero-downtime migration: legacy keeps running throughout
- ✅ Per-context cutover: blast radius scoped, not all-at-once
- ✅ Shadow mode catches calculation discrepancies before they affect beneficiaries
- ✅ Reversible at every phase (flip read/write back to legacy)
- ✅ `source_legacy_isn` preserves audit trail forever
- ❌ **Complex**: multiple moving parts (CDC reader, message bus, dual-write logic). Significant engineering investment in CDC reader for Adabas
- ❌ **Long duration**: at least 6 months from Phase 0 to Phase 4 in real deployment
- ❌ **Higher infrastructure cost** during dual-run (both DBs hot)
- ❌ Adabas CDC requires either log-mining (custom) or triggers (DBA-resistant) — **biggest technical risk**

### Option 3 — Permanent coexistence (federation)

- ✅ No cutover ever needed
- ❌ **Doubles operational cost forever** — running two systems indefinitely
- ❌ Routing layer becomes a critical bottleneck and single point of failure
- ❌ Federation queries (e.g., "list payments across both systems") are slow and complex
- ❌ Defeats the modernization goal

### Option 4 — Shadow read (new for new, legacy for old)

- ✅ Simple: PostgreSQL grows from "today onwards", historical reads proxy to legacy
- ❌ **Reports become a mess**: a "yearly report" must query both systems and merge
- ❌ Beneficiary search must scan both — performance suffers
- ❌ Legacy never goes away — same drawback as Option 3
- ❌ Doesn't solve the migration problem, just defers it

## Consequences

### Positive

- Legacy keeps running until proven obsolete → operational continuity guaranteed
- Per-context cutover reduces blast radius from 100% to ~25% at any moment
- Shadow mode (Phase 2) is a **strong gate** — calculation bugs surface against real production data, not synthetic tests
- Every migrated row is linkable to its source via `source_legacy_isn` → forever audit-ready
- ADR-003 (rounding flag `LEGACY_TRUNCATE`) and shadow mode work together: equivalence is testable
- Workshop produces a credible **plan** + small POC, not a fake "we migrated 180M rows in 8h" claim

### Negative

- **Engineering complexity**: CDC reader for Adabas is custom work (Adabas doesn't have built-in CDC like PostgreSQL's WAL)
- **6-month real-world timeline** to full cutover — slow for stakeholders pushing for "modern system now"
- **Dual infrastructure cost** during Phases 1-4: both Adabas and PostgreSQL run hot, paid in parallel
- **Operator cognitive load**: during transition, operators may need to know "which system answers what" until full cutover per context
- **CDC lag risk**: if CDC falls behind during a busy month, new system reads stale data. Mitigation: hard SLO of CDC lag ≤ 5 minutes, alerts at 10 min

### Neutral

- Schema design slightly bigger (every table has `source_legacy_isn`) — negligible storage cost

## Validation

- ✅ **Workshop POC**: 10K beneficiaries loaded via the ETL, count + checksums match a known fixture
- ✅ **Schema test**: every business table has `source_legacy_isn BIGINT NOT NULL` and index on it
- ✅ **Source link test**: `SELECT b.cpf, b.source_legacy_isn FROM beneficiary b LIMIT 100` returns mappable Adabas ISN values for a known fixture
- ✅ **Comparison framework test**: given the same input (beneficiary + program + competence), legacy fixture and new calculation produce equal payment amount in `LEGACY_TRUNCATE` mode (links back to ADR-003)
- ✅ **Migration runbook** documented at `02-spec-moderna/runbooks/data-migration.md` covers Phases 0-5 with detailed rollback steps per phase
- ✅ **CDC lag metric** defined: `cdc_lag_seconds` Micrometer gauge, alerts at 600s (CRITICAL)
- ✅ **Phase gate criteria** documented: explicit success metrics required before promoting one phase to the next

## Related Requirements

- OUT-008: Migration ETL — covered by ADR-006 strategy
- FR-AUD-004: 10-year retention preserved by carrying `audit_event` forward
- NFR-AVAIL-001: 99.5% SLA preserved during migration (legacy stays up)
- NFR-COMP-002: Audit retention 10 years (covered by audit migration)
- ADR-003: `LEGACY_TRUNCATE` mode enables shadow validation
- ADR-004: FATOR-K parameterization makes the shadow comparison deterministic

## References

- [`techstack.md`](techstack.md) — section 6, decision #5
- [Strangler Fig Application — Martin Fowler](https://martinfowler.com/bliki/StranglerFigApplication.html)
- Sam Newman, *Monolith to Microservices* — Chapter 3, migration patterns
- [Debezium](https://debezium.io/) — reference CDC platform (not used for Adabas but pattern is the same)
- ADABAS WORK file documentation (legacy CDC source)
- [Azure Service Bus](https://learn.microsoft.com/azure/service-bus-messaging/) — message transport for CDC stream

---
