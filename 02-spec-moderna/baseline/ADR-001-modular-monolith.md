<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001: Adopt Modular Monolith over Microservices

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY Foundational](https://img.shields.io/badge/SEVERITY-Foundational-F25022?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture (Enterprise Architect + Software Architect)
- Reviewed by: Par 1 · Vision (Product Owner) for scope alignment
- Reviewed by: Par 3 · Implementation (Tech Lead) for feasibility

## Technical Story

Foundational architecture decision for SIFAP 2.0. Drives all subsequent ADRs and the structure of every bounded context. Related to CON-002 in [requirements.md](requirements.md).

## Context and Problem Statement

SIFAP serves ~4.2M active beneficiaries and processes ~3.8M payments/month (target volume after migration). The legacy is a single Natural/Adabas application on z/OS — already a monolith, but unstructured (15 programs sharing 4 DDMs without clear boundaries, with internal duplication like the CALCBENF/BATCHPGT formula duplication identified as BONUS-02 in Stage 1).

We need an architecture that:

1. Fits the workshop budget (8 hours for full stack 1→2→3→4)
2. Preserves all 71 business rules with traceability to legacy
3. Allows independent team work on the 4 proposed bounded contexts (BeneficiaryManagement, SocialProgramRegistry, PaymentProcessing, ReportingAndAudit)
4. Sustains the production volume without becoming a "big ball of mud"
5. Preserves the option to extract microservices later if growth demands

The question is **whether to adopt microservices, a modular monolith, or a classic layered monolith** as the target architecture.

## Decision Drivers

- **D1 — Workshop timebox**: 8 hours total, ~2 hours for implementation
- **D2 — Team size**: 5 people in 5 pairs, all touching one codebase
- **D3 — Operational complexity budget**: minimal — no SRE team in a workshop
- **D4 — Business rule traceability**: must preserve `source_legacy` for every requirement
- **D5 — Future migration path**: must not lock us out of microservices if scale demands
- **D6 — Volume**: 4.2M beneficiaries, 180M payment records, growing 3.8M/month
- **D7 — Deployment frequency**: monthly batch cycle dominates — daily deploys not required

## Considered Options

1. **Microservices** — one service per bounded context (4 services), independent deployment
2. **Modular Monolith** — single deployable, internal module boundaries enforced by package structure and architecture tests
3. **Classic Layered Monolith** — single deployable, organized by technical layer (controller/service/repository) without explicit bounded contexts

## Decision

**Chosen option: Modular Monolith.**

A single Spring Boot application organized **by feature** (package-by-feature), with each bounded context owning its own `domain`, `application`, `infrastructure`, and `interfaces` packages. Cross-context communication happens via published interfaces or domain events (Spring Application Events) — never direct entity sharing.

Boundaries enforced by:

- ArchUnit tests forbidding cross-package imports outside published APIs
- Code review checklist requiring ADR for any new cross-context dependency
- Separate Flyway migration folders per bounded context (`db/migration/beneficiary/`, etc.)

## Pros and Cons of the Options

### Option 1 — Microservices

- ✅ True team autonomy (independent deploys per context)
- ✅ Natural scale boundaries (PaymentProcessing can scale independently from Reporting)
- ✅ Polyglot persistence possible if a context needs different storage
- ❌ **Operational complexity explodes**: service discovery, distributed tracing, eventual consistency, network failures, sagas for cross-context transactions
- ❌ **Way over the workshop budget**: 4 services × CI/CD pipeline × Terraform module × observability = 16+ artifacts to ship in 2h
- ❌ **Premature distribution**: 5-person team doesn't need it; risk of "distributed monolith" anti-pattern (services that must deploy together)
- ❌ **Data migration becomes 4 problems** instead of 1

### Option 2 — Modular Monolith (chosen)

- ✅ Single deployable: one Docker image, one CI/CD pipeline, one observability stack
- ✅ Strong internal boundaries via package structure + ArchUnit, **without** network overhead
- ✅ ACID transactions cross-context when needed (rare but possible)
- ✅ Easier debugging: single stack trace, no correlation across services
- ✅ Clear migration path to microservices later: extract a module → publish as service when boundaries prove stable
- ❌ **Single point of failure for deploys**: a bug in any context blocks the whole release
- ❌ **Discipline required**: easy to slip into cross-context coupling if reviews are lax
- ❌ **Scaling is coarse-grained**: must scale the whole monolith, even if only PaymentProcessing is hot

### Option 3 — Classic Layered Monolith

- ✅ Familiar to most developers
- ✅ Fastest to start (no upfront design of boundaries)
- ❌ **No protection against the original SIFAP failure mode**: the legacy is a classic layered monolith and ended up with formula duplication (BONUS-02), magic constants (MYS-003), and silent cross-feature side effects (MYS-001, MYS-010)
- ❌ **Long-term unmaintainable**: tightly coupled code grows into a ball-of-mud that nobody dares to change
- ❌ **No future microservices path**: extracting a service from a layered monolith requires rewriting the layer boundaries first

## Consequences

### Positive

- One deployable artifact → simple CI/CD, simple ops
- Bounded contexts enforced **structurally** (ArchUnit) rather than only by convention → resists rot
- Cross-context transactions remain possible where business demands (e.g., payment + audit)
- Migration path to microservices preserved: if PaymentProcessing needs independent scale, extract it cleanly because its boundary is already enforced
- All 5 pairs work in the same repo with clear ownership: each pair has its own bounded context (mostly)

### Negative

- **Scaling is monolithic**: cannot scale only PaymentProcessing independently. Mitigation: vertical scale + read replicas on PostgreSQL until extraction is justified
- **Build time grows with codebase**: a full build takes longer than 4 small services. Mitigation: Gradle incremental builds, run tests per module in CI
- **Discipline burden**: developers must respect package boundaries. Mitigation: ArchUnit tests fail the build on violations
- **Single failure domain at deploy time**: a critical bug in any context can roll back all changes. Mitigation: feature flags for risky changes, blue-green deployment

### Neutral

- Database is shared (one PostgreSQL instance) but **logically partitioned** by Flyway folders per context — same trade-off either way

## Validation

- ✅ ArchUnit tests in CI verify no cross-package imports outside published APIs
- ✅ Flyway migration folders separated by context (`db/migration/{beneficiary,program,payment,audit}/`)
- ✅ Each bounded context has its own integration test suite (`@SpringBootTest(classes = BeneficiaryConfig.class)`)
- ✅ Performance test on shared DB demonstrates ≥ 100 RPS sustained across all contexts (NFR-PERF-001)
- ✅ Documentation in C4 L1/L2 diagrams (Stage 2 deliverable) shows the 4 contexts as logical components, not separate containers

## Related Requirements

- CON-002: Architecture is Modular Monolith — not microservices
- FR-PAY-018: Single `PaymentCalculatorService` (eliminates BONUS-02 duplication)
- NFR-MAIN-002: Every bounded context follows package-by-feature
- NFR-MAIN-003: No financial calculation duplication (validated by ArchUnit)
- NFR-SCAL-002: Stateless horizontal scaling

## References

- [`modular-monolith.instructions.md`](../../.github/instructions/modular-monolith.instructions.md) — package-by-feature rules
- [`techstack.md`](techstack.md) — section 1, principle #1
- [`requirements.md`](requirements.md) — CON-002
- [Discovery Report §3.2](../../01-arqueologia/output-requisitos/discovery-report.final.md) — complex dependencies analysis that justifies bounded contexts
- Sam Newman, *Monolith to Microservices*, O'Reilly 2019 — Strangler Fig pattern, when to extract
- Simon Brown, *Software Architecture for Developers* — Modular Monolith chapter

---
