# SIFAP 2.0 Constitution

> **Status**: Ratified · **Version**: 1.0.0 · **Ratified**: 2026-05-20 · **Last Amended**: 2026-05-20

This constitution is the immutable contract that governs every Spec-Kit prompt (`/speckit.specify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.implement`) and every code review in the SIFAP 2.0 modernization. It distills the [architectural baseline](../../02-spec-moderna/baseline/README.md) into seven non-negotiable principles plus the operational constraints that protect them.

When a principle in this constitution conflicts with a personal preference, a third-party tutorial, or even a Spec-Kit suggestion — **the constitution wins**.

---

## Core Principles

### I. Legacy Traceability (NON-NEGOTIABLE)

Every functional requirement, every spec, every test must carry a `source_legacy:` line pointing to one of:

- A specific `.NSN` program in `01-arqueologia/legado-sifap/natural-programs/` (ideally with a line range), OR
- A specific `.ddm` file in `01-arqueologia/legado-sifap/adabas-ddms/`, OR
- The literal string `[GREENFIELD]` followed by a one-line justification.

Specs without `source_legacy:` are rejected by the `legacy-traceability` CI job. This protects the 29 years of business knowledge encoded in the legacy code from being lost in modernization.

**Rationale**: in the previous workshop edition, teams that skipped legacy reading produced specs that satisfied the brief but broke production business rules. This constitution makes the failure impossible to repeat.

### II. Modular Monolith (NON-NEGOTIABLE)

The target architecture is a **single Spring Boot deployable** organized as a Modular Monolith with four bounded contexts (see [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md)):

- `BeneficiaryManagement` (FNR 150)
- `SocialProgramRegistry` (FNR 151)
- `PaymentProcessing` (FNR 152)
- `ReportingAndAudit` (FNR 153)

Microservices are explicitly out of scope. Cross-context dependencies happen via published `*.api` interfaces or domain events (Spring Application Events) — **never** via direct entity sharing, direct repository access, or SQL into another context's tables. ArchUnit tests enforce these boundaries in CI.

**Rationale**: 8-hour workshop budget, 5-person team, 4.2M users — distribution would buy team autonomy we don't need at a cost (operational complexity) we cannot pay. See [ADR-001](../../02-spec-moderna/baseline/ADR-001-modular-monolith.md).

### III. Test-First Development (NON-NEGOTIABLE)

Tests are written **while** implementing, never after. The cycle is:

1. Write the failing test that describes desired behavior
2. Write the minimum code to make the test pass
3. Refactor under green

Coverage gates enforced in CI:

- Backend: **≥ 70% line coverage**, **≥ 60% branch coverage** (Jacoco)
- Frontend: **≥ 60% line coverage** (Vitest + Istanbul)

PRs falling below either threshold are blocked. Equivalence tests against the legacy SIFAP are required for any code that affects financial calculation, validation rules, or audit semantics.

**Rationale**: a financial system serving 4.2M Brazilians cannot rely on post-hoc tests. Equivalence with the legacy must be provable per merge.

### IV. Stack Discipline

The technology stack is fixed and documented in [`techstack.md`](../../02-spec-moderna/baseline/techstack.md):

- Backend: **Java 21 LTS + Spring Boot 3.3 + Spring Data JPA + PostgreSQL 16**
- Frontend: **Next.js 15 (App Router) + TypeScript 5 strict + Tailwind + shadcn/ui**
- Infra: **Terraform with Azure provider `~> 3.x` + Docker + GitHub Actions (OIDC + SHA-pinned)**
- Auth: **OAuth 2.0 + JWT (Spring Security) + Managed Identity for service-to-service**

Any deviation — new library, version upgrade, alternative tool — requires an Architectural Decision Record (ADR) in `02-spec-moderna/baseline/ADR-NNN-*.md` reviewed by Par 2 (Architecture) and Par 3 (Implementation). The 9 forbidden tools listed in `techstack.md` §5 (microservices, GraphQL, MongoDB, Kafka, Kubernetes, React Native, Lombok, OpenFeign, Hibernate Envers) cannot be re-introduced without an ADR that explicitly supersedes the rejection.

**Rationale**: a fixed stack lets all 5 pairs ship code that integrates the same day. Stack drift is the silent killer of team velocity.

### V. Single Source of Truth per Decision

Every architectural decision lives in exactly one place:

- **Tech choices** → [`techstack.md`](../../02-spec-moderna/baseline/techstack.md)
- **Functional requirements** → [`requirements.md`](../../02-spec-moderna/baseline/requirements.md) FR-*
- **Non-functional requirements** → [`requirements.md`](../../02-spec-moderna/baseline/requirements.md) NFR-*
- **Bounded contexts** → [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md)
- **Significant choices with alternatives considered** → ADRs in `02-spec-moderna/baseline/ADR-NNN-*.md`
- **Language-specific conventions** → `.github/instructions/*.instructions.md` (auto-applied by glob)
- **Persona-specific workflows** → `.github/agents/*.agent.md` and `.github/prompts/*.prompt.md`

Duplication of the same decision in multiple places creates drift. When a decision needs to be referenced from many places, link to its source — do not copy.

**Rationale**: drift between docs is what made the legacy `REGRAS-NEGOCIO-2012.md` say one thing while the code did another. We will not repeat that mistake.

### VI. Security & Compliance by Construction

Five inviolable rules:

1. **No secrets in code, configs, env files, or commit messages**. Secrets live exclusively in Azure Key Vault, accessed via Managed Identity. Gitleaks runs in CI.
2. **Sensitive data (CPF, RG, benefit values) never appear unmasked in logs, exports, audit payloads, or any user-facing display**. The single masking pattern is `XXX.XXX.XXX-NN` ([ADR-007](../../02-spec-moderna/baseline/ADR-007-pii-masking-policy.md)). Full CPF view requires explicit privileged action with audited purpose.
3. **CPF backdoors from the legacy system (`VALDOCS` prefix bypass, `VALBENEF` all-zero bypass) are disabled in production**, controlled by feature flag `sifap.cpf.allowTestCpfs`. The application refuses to start if the flag is `true` in the `prod` profile ([ADR-008](../../02-spec-moderna/baseline/ADR-008-cpf-test-backdoors.md)).
4. **All audit events are immutable**: PostgreSQL trigger blocks `UPDATE`/`DELETE` on the `audit_event` table. Retention is 10 years minimum (IN-TCU 63/2010).
5. **OWASP Top 10 compliance is enforced via CI** — OWASP ZAP scan blocks merges with high/critical findings.

LGPD (Lei 13.709/2018) compliance is the floor, not the ceiling.

**Rationale**: 4 of the 24 Stage 1 findings were security/compliance violations tolerated for years. We are using modernization to fix them, not migrate them.

### VII. Observability and Operational Honesty

Every operation that matters must be observable:

- **Logs** are JSON-structured (Logback), shipped to Application Insights
- **Metrics** follow the RED method (Rate, Errors, Duration) for every public endpoint and batch job; exposed via Spring Boot Actuator + Micrometer
- **Traces** use OpenTelemetry; every request carries `X-Correlation-Id` propagated across logs and audit events
- **Alerts** fire on: SLA breach (P95 > limit), error rate > 1%, audit-write failure, batch failure
- **Status of running batch jobs** is queryable via `BATCH_JOB_EXECUTION` table and exposed via admin UI

What we don't observe, we cannot fix. What we don't measure, we cannot improve.

**Rationale**: the legacy `RELAUDIT.NSN` filtered exclusions silently (MYS-010), and `BONUS-06` revealed that consultation actions (`CO`) stopped being logged in 2010 without anyone noticing for 16 years. SIFAP 2.0 makes such omissions structurally impossible.

---

## Architectural Constraints

These constraints are derived from `requirements.md` §4 and apply to every spec:

| CON | Constraint |
|---|---|
| CON-001 | Stack-alvo is Java 21 + Spring Boot 3.3 + Next.js 15 + PostgreSQL 16 + Azure (no deviations without ADR) |
| CON-002 | Architecture is Modular Monolith — not microservices (Principle II) |
| CON-003 | Every REQ-ID requires `source_legacy:` (Principle I) |
| CON-004 | CNAB 240 integration with Banco do Brasil must be preserved (no layout change) |
| CON-005 | SIAFI integration must be preserved |
| CON-006 | Audit trail rejects `UPDATE`/`DELETE` (immutability, IN-TCU 63/2010) |
| CON-007 | Audit retention ≥ 10 years (IN-TCU 63/2010) |
| CON-008 | LGPD compliance: consent, legal basis, right to deletion (Lei 13.709/2018) |
| CON-009 | CPF backdoors disabled in production ([ADR-008](../../02-spec-moderna/baseline/ADR-008-cpf-test-backdoors.md)) |
| CON-010 | Payments are unique by (CPF, competence) |

Any spec that would violate a CON must be rejected by `/speckit.clarify` and either reformulated or escalated to Par 2 for a new ADR.

---

## Development Workflow

### Spec-Kit pipeline (immutable order)

```text
/speckit.constitution   → this document, ratified once
       ↓
/speckit.specify        → spec.md per feature, with source_legacy in every REQ
       ↓
/speckit.clarify        → resolve [NEEDS-CLARIFICATION] markers and CON violations
       ↓
/speckit.plan           → technical plan referencing techstack + bounded-contexts + ADRs
       ↓
/speckit.tasks          → atomic, ordered tasks (one PR per task ideally)
       ↓
/speckit.analyze        → consistency + coverage check before implementation
       ↓
/speckit.implement      → code + tests + docs, in package defined by bounded-contexts.md
```

Skipping a stage is forbidden. Failing analysis (`/speckit.analyze`) blocks `/speckit.implement`.

### Branch strategy

- `main` — protected, requires ≥ 1 peer review
- `develop` — integration branch
- Per stage: `visao` → `arquitetura` → `implementacao` → `qualidade` → `operacoes`
- Per spec: `spec/<NNN>-<feature>` (created by `/speckit.specify`)
- Merge order: `spec/*` → `develop` → `stage-named` → `main`

### Quality gates (must pass to merge)

1. ✅ All tests green (unit + integration + E2E)
2. ✅ Coverage thresholds met (Principle III)
3. ✅ `legacy-traceability` CI job green (Principle I)
4. ✅ ArchUnit tests green (Principle II — no cross-context coupling)
5. ✅ OWASP ZAP scan: 0 high/critical (Principle VI)
6. ✅ Gitleaks: 0 secrets detected (Principle VI)
7. ✅ Lighthouse CI ≥ 90 for any frontend change (NFR-USAB-003)
8. ✅ Code review approved by ≥ 1 peer

### Documentation discipline

- Every change to a public API or domain model updates the relevant spec
- Every architectural decision creates or amends an ADR
- ADRs are immutable for the decision; only the validation section can be updated
- New ADRs supersede old ones explicitly (`Status: Superseded by ADR-XXX`)

---

## Governance

### Authority

This constitution supersedes:

- Personal preferences and style choices
- Tutorials, blog posts, third-party recommendations
- Default behaviors of Copilot, Spec-Kit, or any tool

When a tool suggests something incompatible with this constitution, the constitution wins. When in doubt: link to the relevant principle, ADR, or requirement.

### Amendment process

Changes to this constitution require:

1. PR with explicit rationale referencing one or more Stage 1 findings, ADRs, or requirements
2. Review by Par 2 (Architecture) — Enterprise Architect + Software Architect
3. Review by Par 1 (Vision) — Product Owner — if the change affects scope or compliance
4. Migration plan if the change impacts existing specs (typically a new ADR documenting the transition)
5. Update of `Last Amended` date below
6. Version bump per semantic versioning:
   - **MAJOR** — backward-incompatible change to a principle (e.g., adopting microservices)
   - **MINOR** — new principle or significant scope expansion
   - **PATCH** — clarifications, typo fixes, link updates

### Enforcement

- `/speckit.constitution` re-reads this file for every Spec-Kit invocation
- CI jobs enforce Principles I (traceability), II (ArchUnit), III (coverage), VI (security)
- Code review checklist (template in `.github/PULL_REQUEST_TEMPLATE.md`) requires reviewer to confirm constitutional compliance
- Stage handoff meetings (H1, H2, H3 — see `00-TEAM-FLOW.md`) include constitution sanity check

### Conflict resolution

If two principles appear to conflict in a specific case:

1. The **non-negotiable** principle (marked NON-NEGOTIABLE) wins
2. If both are non-negotiable, escalate to Par 2 + Par 1 joint decision documented in an ADR
3. The ADR may amend this constitution if the resolution applies broadly

---

## References

- [Architectural baseline](../../02-spec-moderna/baseline/README.md) — entry point for all decisions referenced here
- [Tech stack](../../02-spec-moderna/baseline/techstack.md) — Principle IV detail
- [Requirements](../../02-spec-moderna/baseline/requirements.md) — FRs and NFRs governed by these principles
- [Bounded contexts](../../02-spec-moderna/baseline/bounded-contexts.md) — Principle II detail
- [ADRs Index](../../02-spec-moderna/baseline/ADRs-INDEX.md) — Principle V (single source) for major decisions
- [Stage 1 archaeology](../../01-arqueologia/) — evidence base for every `source_legacy:` reference
- [Legacy Exploration Checklist](../../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md) — Principle I gate
- [Spec-Kit](https://github.com/github/spec-kit) — pipeline this constitution governs

---

**Version**: 1.0.0 | **Ratified**: 2026-05-20 | **Last Amended**: 2026-05-20

*This constitution was synthesized from the architectural baseline of SIFAP 2.0. It is ratified by Par 2 (Architecture) with sign-off from Par 1 (Product Owner) for scope and Par 3 (Tech Lead) for implementation feasibility. Subsequent amendments are tracked in the project's ADR record.*
