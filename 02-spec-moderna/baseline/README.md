<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Baseline — Architectural Foundation for SIFAP 2.0

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![TIPO Reference Bundle](https://img.shields.io/badge/TIPO-Reference%20Bundle-1A1A1A?style=for-the-badge) ![COUNT 11 docs](https://img.shields.io/badge/COUNT-11%20docs-7FBA00?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Estágio 2](../README.md) → **Baseline**

> **What this folder is**: the architectural baseline that **every Spec-Kit prompt** (`/speckit.constitution`, `/speckit.specify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.implement`) reads as input. Treat it as a contract — changes require review by Par 2 (Architecture).

## Why this folder exists

The Stage 1 archaeology (`01-arqueologia/`) produced 71 business rules, 24 findings, and 4 bounded contexts. Those need to be turned into a **coherent technical contract** before specs can be written.

This `baseline/` bundles three layers of that contract:

| Layer | What it answers | Files |
|---|---|---|
| **Tech stack** | Which technologies, which versions, why | [`techstack.md`](techstack.md) |
| **Requirements** | What the system shall do (FR) and how (NFR) | [`requirements.md`](requirements.md) |
| **Architecture decisions** | Why each significant choice was made | [`ADR-001`](ADR-001-modular-monolith.md) through [`ADR-008`](ADR-008-cpf-test-backdoors.md) + [`ADRs-INDEX.md`](ADRs-INDEX.md) |

Together they answer the three questions Spec-Kit needs to operate:

1. **What technology are we building on?** → `techstack.md`
2. **What must the system do/be?** → `requirements.md`
3. **Why are the non-obvious choices the way they are?** → ADRs

## Catalog

### Foundation

| File | Purpose | Read first when... |
|---|---|---|
| [`techstack.md`](techstack.md) | 22 technologies × version × justification + legacy mapping + what NOT to use | Setting up a new project, evaluating a new dependency |
| [`requirements.md`](requirements.md) | 48 FRs + 40 NFRs + 10 constraints + 8 out-of-scope items, traced to 71 BRs | Writing a spec, validating a feature, prioritizing work |

### Architectural Decision Records (ADRs)

8 ADRs in MADR format, each with ≥3 considered options, honest pros/cons, validation steps, and references.

| # | Title | Severity | Resolves |
|---|---|---|---|
| [001](ADR-001-modular-monolith.md) | Adopt Modular Monolith over Microservices | Foundational | CON-002 |
| [002](ADR-002-pe-groups-mapping.md) | Map Adabas PE Groups to PostgreSQL Child Tables (with JSONB Exception) | High | FR-BEN-005, BR-006 |
| [003](ADR-003-financial-rounding.md) | Financial Rounding — Half-Even with Legacy Compatibility Flag | Critical | MYS-005, INC-004 |
| [004](ADR-004-fator-k-handling.md) | FATOR-K Handling — Parameter Table with Audit | Critical | MYS-003, BR-010 |
| [005](ADR-005-batch-orchestration.md) | Spring Batch + Quartz Inside the Monolith | High | FR-PAY-012, NFR-PERF-003 |
| [006](ADR-006-data-migration-strategy.md) | Strangler Fig with Read-Through Cutover | Critical | OUT-008, NFR-AVAIL-001 |
| [007](ADR-007-pii-masking-policy.md) | PII Masking — `XXX.XXX.XXX-NN` Single Policy | Critical | BONUS-03, BONUS-04 |
| [008](ADR-008-cpf-test-backdoors.md) | CPF Backdoors — Feature Flag, Forbidden in Prod | High | MYS-007, EGG-002 |

Full index with dependencies, status legend, and coverage mapping: [`ADRs-INDEX.md`](ADRs-INDEX.md).

## How to use with Spec-Kit

### 1. `/speckit.constitution`

Reference the entire baseline when generating the project constitution. Suggested prompt:

```text
/speckit.constitution Generate a constitution for SIFAP 2.0 based on the
architectural baseline in 02-spec-moderna/baseline/. Pull principles from
techstack.md section 1, decision discipline from ADRs-INDEX.md, and
non-negotiable constraints from requirements.md section 4.
```

### 2. `/speckit.specify`

Reference the requirements and relevant ADRs for the feature being specified. Suggested prompt:

```text
/speckit.specify Beneficiary Registration. Input documents:
- Functional requirements: 02-spec-moderna/baseline/requirements.md section 2.1 (FR-BEN-001 to FR-BEN-013)
- Source legacy: 01-arqueologia/output-requisitos/business-rules-catalog.final.md BR-001 to BR-012
- Applicable ADRs: ADR-002 (PE groups), ADR-007 (PII masking), ADR-008 (CPF backdoors)
- Constraints: 02-spec-moderna/baseline/requirements.md section 4 (CON-001, CON-003, CON-008)
```

### 3. `/speckit.plan`

Pull tech stack and ADRs into the plan. Suggested prompt:

```text
/speckit.plan Use the stack defined in 02-spec-moderna/baseline/techstack.md.
Honor the architectural decisions in 02-spec-moderna/baseline/ADR-*. Tests
must follow .github/instructions/tests.instructions.md.
```

### 4. `/speckit.clarify` and `/speckit.tasks`

These iterate on the spec generated in step 2. They don't need to re-read the baseline directly — the spec already encoded what was needed.

### 5. `/speckit.implement`

The implementation phase reads back to the ADRs whenever a non-obvious design choice is needed during coding:

```text
/speckit.implement Implement BeneficiaryRegistrationService for spec 001.
If anything is unclear about rounding, refer to ADR-003. For CPF masking,
use the pattern from ADR-007 (BrazilianCpfMasker utility).
```

## Suggested reading order

For a new team member or a new Spec-Kit session, read in this order:

1. **[`techstack.md`](techstack.md)** (15 min) — what are we building on
2. **[`requirements.md`](requirements.md)** sections 1–2 (20 min) — what must the system do
3. **[`ADRs-INDEX.md`](ADRs-INDEX.md)** (5 min) — overview of decisions
4. **[`ADR-001`](ADR-001-modular-monolith.md)** (10 min) — foundational architecture
5. **[`ADR-003`](ADR-003-financial-rounding.md)** + **[`ADR-007`](ADR-007-pii-masking-policy.md)** (15 min) — financial and compliance critical decisions
6. **Remaining ADRs as needed** when working on the relevant context

Total: ~1 hour for full baseline; 30 min for the critical path.

## Versioning and governance

| Operation | Requires |
|---|---|
| **Update `techstack.md`** (add/change a dependency) | New ADR + PR review by Par 2 + Par 3 |
| **Update `requirements.md`** (new FR or NFR) | PR with traceability to source (BR-XXX or `[GREENFIELD]`) + Par 1 approval for scope |
| **Add a new ADR** | Follow process in [`ADRs-INDEX.md`](ADRs-INDEX.md) "How to add a new ADR" |
| **Change an existing ADR's decision** | Don't. Create a new ADR with status `Superseded by ADR-XXX` |
| **Update validation section of an ADR** | Simple PR (no architecture review needed) |
| **Move/rename a file in this folder** | Coordinate with Par 2 — breaks Spec-Kit prompt references |

## Coverage check

| Stage 1 artifact | Stage 2 baseline coverage |
|---|---|
| 71 business rules (`business-rules-catalog.final.md`) | ✅ All referenced in `requirements.md` FRs |
| 24 findings (`mysteries-found.final.md`) | ✅ All addressed in FR, NFR, or ADR (see ADRs-INDEX coverage table) |
| 4 bounded contexts (`discovery-report.final.md`) | ✅ Drive `requirements.md` section 2 organization |
| 22 legacy artifacts (15 NSN + 4 DDM + 3 docs) | ✅ Each referenced via `source_legacy:` in BRs |

This baseline is **complete enough** to write the first set of EARS specs. Any gaps will be discovered during `/speckit.clarify` and resolved either by:

- Updating the relevant doc here (preferred for permanent decisions)
- Adding the question to the SENARC list in `requirements.md` section 6

## What this folder is NOT

- ❌ **Not** the specs themselves — those go in `specs/<NNN>-<feature>/` (created by `/speckit.specify`)
- ❌ **Not** the constitution — that goes in `.specify/memory/constitution.md` (created by `/speckit.constitution`, references this folder)
- ❌ **Not** code — that goes in `03-implementacao/` (Stage 3 output)
- ❌ **Not** team worksheets — `scope-decisions.md` and templates stay in `02-spec-moderna/` root

---

## Referências

- [`../README.md`](../README.md) — Stage 2 overview
- [`../GUIDE.md`](../GUIDE.md) — Stage 2 walkthrough
- [`../../01-arqueologia/`](../../01-arqueologia/) — Stage 1 archaeology (input to this baseline)
- [`../../.github/instructions/`](../../.github/instructions/) — language/context conventions (auto-applied by glob)
- [Spec-Kit](https://github.com/github/spec-kit) — official SDD plugin
- [MADR](https://adr.github.io/madr/) — ADR format used here

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="../README.md"><strong>Estágio 2 — README</strong></a><br/>
<sub>Stage overview.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="techstack.md"><strong>Tech Stack</strong></a><br/>
<sub>Start with the foundation.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../../README.md">Voltar ao Kit PT-BR</a></sub>
