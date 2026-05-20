<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002: Map Adabas PE Groups to PostgreSQL Child Tables (with JSONB Exception)

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY High](https://img.shields.io/badge/SEVERITY-High-FFB900?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture (Enterprise Architect + Software Architect)
- Reviewed by: Par 4 · Quality (DBA + QA Engineer) — owns schema migration
- Reviewed by: Par 3 · Implementation (Tech Lead) — affects JPA model

## Technical Story

Legacy uses Adabas Periodic Groups (PE) extensively for one-to-many relationships in the same physical record:

| DDM | PE Group | Max Occurrences | Stage 1 Finding |
|---|---|---|---|
| `BENEFICIARIO.ddm` | DEPENDENTS | 10 (DDM) / 5 (code) / 3 (manual) | MYS-002 / INC-001 (triple mismatch) |
| `BENEFICIARIO.ddm` | DESCONTOS (discounts) | 8 | BR-007 |
| `PROGRAMA-SOCIAL.ddm` | FAIXA-CALCULO (calc bands) | 5 | BR-015 |
| `PROGRAMA-SOCIAL.ddm` | PARAM-REGIONAL (regional params) | 6 | BR-012 |
| `AUDITORIA.ddm` | CAMPO-ALTERADO-ANT/DEP (audit fields) | 20 (MU+PE combination) | BR-038 |

These structures don't exist in relational PostgreSQL. We need a single, consistent policy.

## Context and Problem Statement

PostgreSQL offers multiple ways to represent one-to-many relationships:

- **Child tables** with FK back to parent — relational standard
- **JSONB column** — semi-structured, queryable via `jsonb_path_ops`
- **PostgreSQL arrays** (`int[]`, `text[]`) — typed but limited
- **Composite types** — rarely used in practice

Each has different trade-offs for query patterns, index strategy, and JPA mapping. The decision affects performance of high-volume queries (180M payment records joined with discounts) and modeling clarity.

The decision needs to balance:

- Query-ability (filter, sort, aggregate on PE values)
- Performance at scale (180M payments, 4.2M beneficiaries)
- Schema evolution (adding fields to a PE group later)
- JPA mapping complexity
- Migration ease from Adabas

## Decision Drivers

- **D1 — Query pattern**: do we filter/sort/aggregate by PE values? (varies per group)
- **D2 — Cardinality**: how many child records per parent? (varies: 5–20)
- **D3 — Schema stability**: how often will fields be added/removed?
- **D4 — Volume**: 4.2M beneficiaries × ~3 dependents each = ~12M rows in child table
- **D5 — Indexing strategy**: relational FK index vs GIN index on JSONB
- **D6 — JPA mapping ergonomics**: `@OneToMany` is more idiomatic than `@JdbcTypeCode(SqlTypes.JSON)`
- **D7 — Auditability**: changes to individual PE items are easier to track in a child table

## Considered Options

1. **All child tables** — every PE group becomes a separate table with FK
2. **All JSONB columns** — every PE group becomes a `jsonb` column on the parent
3. **Hybrid by query pattern** — child tables for queryable PE groups, JSONB for write-mostly / display-only
4. **PostgreSQL arrays** — typed arrays for simple PE groups (e.g., MU `TIPO-DSCT-APLIC` as `text[]`)

## Decision

**Chosen option: 3 — Hybrid by query pattern.**

Rule:

- **Child table** when: we need to filter/aggregate/sort by PE values, OR fields evolve independently, OR auditing individual items matters
- **JSONB column** when: PE is read whole + replaced whole, no per-item queries, schema unstable
- **PostgreSQL array** when: simple typed list, no per-item attributes (e.g., applicable discount types)

### Concrete mapping per PE group

| PE Group | Mapping | Reason |
|---|---|---|
| `BENEFICIARIO.DEPENDENTS` | **Child table** `dependent` (FK `beneficiary_id`) | Queryable (search by dependent CPF), cardinality 0–10, audited individually (FR-BEN-009) |
| `BENEFICIARIO.DESCONTOS` | **Child table** `beneficiary_discount` (FK `beneficiary_id`) | Filter by `tipo_desconto`, vigência queries, audited (BR-013, BR-023, BR-038) |
| `PROGRAMA-SOCIAL.FAIXA-CALCULO` | **JSONB column** `calculation_bands` | Read whole when calculating, replaced as a unit when program changes, rarely 5 items, no per-band queries needed (BR-015) |
| `PROGRAMA-SOCIAL.PARAM-REGIONAL` | **JSONB column** `regional_params` | Same: read whole, replaced whole, no per-region filter on programs (BR-012) |
| `PROGRAMA-SOCIAL.TIPO-DSCT-APLIC` (MU) | **PostgreSQL `text[]`** `applicable_discount_types` | Simple typed list, no per-item attributes |
| `AUDITORIA.CAMPO-ALTERADO-ANT/DEP` (MU) | **JSONB column** `before_state`, `after_state` | Audit payload is semi-structured by nature; queries are by event metadata, not by field name inside payload |

### JPA examples

```java
// Child table case
@Entity
class Beneficiary {
  @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Dependent> dependents;

  @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BeneficiaryDiscount> discounts;
}

// JSONB case
@Entity
class SocialProgram {
  @Column(columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private List<CalculationBand> calculationBands;
}

// Array case
@Column(columnDefinition = "text[]")
private List<String> applicableDiscountTypes;
```

## Pros and Cons of the Options

### Option 1 — All child tables

- ✅ Maximum query-ability: any PE attribute is filterable, sortable, aggregable
- ✅ Standard FK index strategy (cheap for any DBA)
- ✅ Audit trail per row is trivial
- ❌ **Schema bloat**: 5 PE groups × parent tables = 5 extra tables for sometimes-trivial structures
- ❌ **JPA boilerplate**: every PE becomes another `@OneToMany` to manage
- ❌ **JOIN cost** when reading parent + all PE groups: 5 joins for a single beneficiary
- ❌ Overkill for write-mostly PE groups (audit before/after state)

### Option 2 — All JSONB columns

- ✅ Single SELECT reads everything (no joins)
- ✅ Flexible schema (add fields without migration)
- ✅ Compact storage
- ❌ **Hard to query at scale**: `WHERE dependents @> '[{"cpf": "..."}]'` works but is slower than indexed FK
- ❌ **Audit per item is awkward**: must diff entire JSON blob to track which dependent changed
- ❌ **Per-row constraints impossible**: can't enforce "dependent CPF must validate Mod 11" via DB
- ❌ **GIN index size** for JSONB columns with high cardinality becomes large

### Option 3 — Hybrid by query pattern (chosen)

- ✅ Each PE group uses the right tool for its actual usage
- ✅ Hot paths (dependent search, discount filter) use indexed FK joins
- ✅ Cold paths (audit payload, calc bands) avoid table proliferation
- ✅ Aligns with PostgreSQL idioms: child tables for relational data, JSONB for semi-structured
- ❌ **Requires per-PE judgment** — slightly more design effort upfront
- ❌ **Two mental models**: developers must remember which PE is which
- ❌ **Slightly more complex schema migration scripts**

### Option 4 — PostgreSQL arrays everywhere

- ✅ Typed and indexable (GIN with `array_ops`)
- ❌ **No nested attributes**: can't have `dependents` as `text[]` because each dependent has name + CPF + birth date — needs a composite type or hashed-into-string trick
- ❌ Only fits the simplest cases (already covered as exception in Option 3)

## Consequences

### Positive

- Common case (`dependent`, `discount`) is queryable, indexable, auditable — matches what BR-007, BR-008, BR-023, BR-038 actually need
- JSONB used only where it adds value (calc bands, audit payload) — no schema bloat
- JPA mapping stays idiomatic (`@OneToMany` for relational, `@JdbcTypeCode(SqlTypes.JSON)` for semi-structured)
- Performance: dependent search uses `beneficiary_id` index (B-tree, fast); program load uses single SELECT with JSONB columns
- Migration from Adabas is direct: PE → unnest into child table OR serialize into JSONB based on the rule

### Negative

- **Two different patterns to remember**: developers reading a new entity for the first time need to check the model to know which approach is used
- **Migration scripts have two flavors**: ETL must handle both child-table inserts and JSONB serialization
- **Constraint asymmetry**: dependent CPF can be `CHECK`-constrained at DB level; calc bands inside JSONB rely on application validation

### Neutral

- Storage cost: child table approach has slight overhead for FK columns, JSONB has slight overhead for keys repeated per row — wash at SIFAP scale

## Validation

- ✅ ArchUnit test verifies every `@OneToMany` is `cascade = CascadeType.ALL, orphanRemoval = true` (matches PE group semantics: dependent has no life without beneficiary)
- ✅ JPA query test demonstrates filter on `dependent.cpf` via FK index (EXPLAIN shows `Index Scan` not `Seq Scan`)
- ✅ JPA query test demonstrates JSONB read of `calculation_bands` returns whole list in single query (EXPLAIN shows `Index Scan on social_program_pkey`)
- ✅ Migration script test: seed 1000 beneficiaries with 3 dependents each → query "find beneficiary with dependent CPF X" returns in < 20ms
- ✅ Code review checklist includes: "If adding a new PE-like field, document the choice (child table vs JSONB vs array) and reason in the entity Javadoc"

## Related Requirements

- FR-BEN-005: Dependents as child entity (FK to `beneficiary`)
- FR-PROG-001: Social program with calculation bands and regional params
- FR-AUD-001: Immutable audit trail
- NFR-PERF-001: P95 ≤ 300ms for beneficiary queries
- NFR-PERF-005: Payment history query P95 ≤ 200ms (depends on FK index on `payment.cpf, competence`)
- NFR-COMP-003: Audit trail immutable (JSONB before/after state)

## References

- [`database.instructions.md`](../../.github/instructions/database.instructions.md)
- [`techstack.md`](techstack.md) — Mapping legacy → modern (PE groups row)
- [`mysteries-found.final.md`](../../01-arqueologia/output-requisitos/mysteries-found.final.md) — MYS-002 (triple mismatch on dependent limit)
- [PostgreSQL JSONB documentation](https://www.postgresql.org/docs/16/datatype-json.html)
- [Hibernate 6.x JSON mapping](https://docs.jboss.org/hibernate/orm/6.4/userguide/html_single/Hibernate_User_Guide.html#mapping-column-jdbctypecode)
- Vlad Mihalcea, *High-Performance Java Persistence* — chapters on `@OneToMany` and JSON types

---
