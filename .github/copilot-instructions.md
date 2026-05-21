# Copilot Instructions — Workshop SIFAP Modernization

Workshop project modernizing **SIFAP** legacy (Natural/Adabas, 29 years) to Java 21 + Spring Boot 3.3 + Next.js 15 + PostgreSQL 16, using GitHub Spec-Kit and Copilot agentic workflows.

## Project Structure

- `01-arqueologia/legado-sifap/` — read-only legacy (15 `.NSN` programs + 4 `.ddm` DDMs + 3 historic docs)
- `01-arqueologia/*.md` — HARD GATE artifacts (glossary, business-rules-catalog, dependency-map, mysteries-found, discovery-report)
- `01-arqueologia/output-requisitos/*.final.md` — extended versions of HARD GATE artifacts (this team's convention)
- `02-spec-moderna/` — EARS specs + ADRs (Stage 2 output)
- `03-implementacao/` — Java backend + Next.js frontend (Stage 3)
- `04-evolucao/` — Agent delegation reports (Stage 4)
- `05-personas/NN-*/` — persona kits to copy into `.github/` (one per role)
- `06-agentes-de-estagio/` — stage agents (`@archaeologist`, `@architect`, `@builder`, `@evolution`)
- `09-cheat-sheets/` — Copilot modes, Spec-Kit workflow, model routing

## Stack & Code Conventions

Code generation rules live in `.github/instructions/*.instructions.md` (auto-applied by glob):

- Java/Spring → [backend.instructions.md](instructions/backend.instructions.md)
- TypeScript/Next.js → [frontend.instructions.md](instructions/frontend.instructions.md) + [frontend-spec.instructions.md](instructions/frontend-spec.instructions.md)
- PostgreSQL → [database.instructions.md](instructions/database.instructions.md)
- Terraform → [infrastructure.instructions.md](instructions/infrastructure.instructions.md)
- CI/CD → [cicd.instructions.md](instructions/cicd.instructions.md)
- Tests → [tests.instructions.md](instructions/tests.instructions.md)
- Security/OWASP → [security.instructions.md](instructions/security.instructions.md)
- Modular monolith patterns → [modular-monolith.instructions.md](instructions/modular-monolith.instructions.md)
- Reading legacy Natural/Adabas → [natural-adabas.instructions.md](instructions/natural-adabas.instructions.md)
- EARS requirements → [requirements.instructions.md](instructions/requirements.instructions.md)

## Spec-Driven Development — Hard Rule

Every requirement in `02-spec-moderna/` MUST carry a `source_legacy:` line pointing to:
- A specific `.NSN` program in `01-arqueologia/legado-sifap/natural-programs/` (with line range), OR
- A specific `.ddm` file in `01-arqueologia/legado-sifap/adabas-ddms/`, OR
- The literal string `[GREENFIELD]` with a 1-line justification.

The `legacy-traceability` CI job rejects PRs missing this. See [LEGACY-EXPLORATION-CHECKLIST.md](../01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md).

## Two-Layer Agent System

Both layers are required — they cover orthogonal axes (role × stage):

1. **Persona kits** (`05-personas/NN-*/`) — installed once per person via `cp -r 05-personas/XX-*/.github/* .github/`. Each kit provides a `.agent.md` + `.prompt.md` files (slash commands like `/ears-convert`) + skills.
2. **Stage agents** (`06-agentes-de-estagio/`) — `@archaeologist` (Stage 1), `@architect` (Stage 2), `@builder` (Stage 3), `@evolution` (Stage 4). Selected in Copilot Chat per stage.

See [00-TEAM-FLOW.md](../00-TEAM-FLOW.md) for handoff diagrams and timeline.

## Team Composition (5 pairs × 2 personas)

| Pair | Persona A | Persona B | SDLC Phase |
|------|-----------|-----------|------------|
| 1 · Vision | Product Owner | Requirements Engineer | Discovery + Spec |
| 2 · Architecture | Enterprise Architect | Software Architect | Spec + Design |
| 3 · Implementation | Tech Lead | Developer | Implementation |
| 4 · Quality | DBA | QA Engineer | Implementation (data + tests) |
| 5 · Operations | DevOps Engineer | Tech Writer | Cross-cutting + Evolution |

## Branch Strategy

- One branch per stage/spec: `visao` → `arquitetura` → `implementacao` → `qualidade` → `operacoes`
- For specs: `spec/<NNN>-<feature>`
- Merge order: `spec/*` → `develop` → `stage` → `main`
- **Never merge to `main` without peer review.**

## Copilot Mode Routing

| Mode | When | Example |
|------|------|---------|
| **Ask** | Explore, debate trade-offs | "Explain this Natural program" |
| **Plan** | Multi-file change before execution | "Plan the `notification` bounded context" |
| **Agent** | Delegate complete feature via Issue | "Implement REQ-PAY-03" |

Full guide: [09-cheat-sheets/copilot-3-modes.md](../09-cheat-sheets/copilot-3-modes.md)

## Approved Tools — Toolchain is Fixed

**Use only**: VS Code (+ Insiders), GitHub Copilot (Ask/Plan/Agent), GitHub Copilot CLI, GitHub Spec-Kit (`Specify CLI` + `/speckit.*`), GitHub Issues/PRs/Actions, Docker, Terraform.

**Do not use**: Cursor, Windsurf, Antigravity, Codex, Cline, Continue, Aider, Codeium, Tabnine, IntelliJ/Eclipse/Sublime/Neovim as primary editor, web chat UIs for code generation, or alternative SDD frameworks. Mixing tools breaks team handoffs and traceability.

## Hard Rules — Don't Do This

- ❌ Don't write an EARS without `source_legacy:` — CI will reject the PR
- ❌ Don't add dependencies without ADR justification
- ❌ Don't write tests after the fact — write them while implementing
- ❌ Don't expose secrets in commit messages, logs, or PR descriptions
- ❌ Don't merge to `main` without peer review
- ❌ Don't skip the guided handoff conversations between stages (see [00-TEAM-FLOW.md](../00-TEAM-FLOW.md))
- ❌ Don't bypass legacy exploration — specs without it lose 29 years of business rules
- ❌ Don't mask production data (CPF, benefit values) inconsistently — use a single LGPD-compliant pattern

## References

- [Team Flow](../00-TEAM-FLOW.md) — daily timeline, handoffs, escalation rules
- [Setup](../00-SETUP.md) — laptop setup, branch protection
- [Persona Kits](../05-personas/) — your two role cards (PERSONA.md)
- [Cheat Sheets](../09-cheat-sheets/) — Copilot, Spec-Kit, model routing
- [Spec-Kit](https://github.com/github/spec-kit) — official SDD plugin

<!-- For per-language conventions, see .github/instructions/*.instructions.md (auto-applied) -->
<!-- For stage workflows, select the matching stage agent in Copilot Chat -->
<!-- Repo memory: /memories/repo/workshop-preto-paula.md tracks current project state -->

<!-- SPECKIT START -->
**Active feature plan**: [`specs/004-eligibility-validation/plan.md`](../specs/004-eligibility-validation/plan.md)
<!-- SPECKIT END -->
