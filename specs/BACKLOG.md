# Feature Backlog — SIFAP 2.0

Status: draft · maintained by Pair 1 (Visão) · synced after every passagem.

Three features have full specs already. The remaining items below trace back to the 15 legacy Natural programs and the bounded contexts defined in [`bounded-contexts.md`](../02-spec-moderna/baseline/bounded-contexts.md). Each entry is a future feature branch (`NNN-slug`) waiting for a Product Owner go-ahead.

## Active feature branches

| # | Branch | Status | Owner | Notes |
|---|---|---|---|---|
| 001 | `001-payment-cycle-generation` | ✅ spec + plan + tasks + impl | Pair 3 | Critical path. Depends on 003. |
| 002 | `002-beneficiary-registration` | ✅ spec + plan + tasks + impl | Pair 4 | Owns CPF rules, lifecycle state machine. |
| 003 | `003-social-program-registry` | ✅ spec + plan + tasks + impl | Pair 2 | **Unblocks 001** — published port + regional factor table. |

## Backlog (P1)

| # | Slug | Bounded Context | Legacy | Why P1 |
|---|---|---|---|---|
| 004 | `004-eligibility-validation` | SocialProgramRegistry | `VALELEG.NSN` | Region-99 bypass (BR-024, MYS-008) lives here. Without this, the cycle accepts everyone. |
| 005 | `005-bank-reconciliation` | PaymentProcessing | `BATCHCON.NSN` | CNAB 240 return processing (BR-036, BR-037). Money is not "paid" until reconciled. |

## Backlog (P2)

| # | Slug | Bounded Context | Legacy | Notes |
|---|---|---|---|---|
| 006 | `006-discount-calculation` | PaymentProcessing | `CALCDSCT.NSN` | 30%-cap rule (BR-013) and judicial exception (MYS-006). |
| 007 | `007-payment-correction` | PaymentProcessing | `CALCCORR.NSN` | Inflation correction (TR + IPCA). Preserves mainframe truncation (BR-020). |
| 008 | `008-document-validation` | BeneficiaryManagement | `VALDOCS.NSN` | The 8-prefix CPF backdoor (BR-028, MYS-007) — partially covered by ADR-008. |
| 009 | `009-payment-reporting` | ReportingAndAudit | `BATCHREL.NSN`, `RELPGT.NSN` | Operator daily/monthly reports. Half-up rounding here (BR-021) diverges from cycle (INC-004) — must be reconciled. |
| 010 | `010-audit-reporting` | ReportingAndAudit | `RELAUDIT.NSN` | 10-year retention queries (NFR-COMP-001). |

## Backlog (P3)

| # | Slug | Bounded Context | Legacy | Notes |
|---|---|---|---|---|
| 011 | `011-beneficiary-query` | BeneficiaryManagement | `CONSBENF.NSN` | Beneficiary self-service portal. Frontend-heavy. Last-12-payments rule (BR-039). |
| 012 | `012-batch-orchestration-ui` | PaymentProcessing | new | Operator dashboard for triggering/monitoring all batch jobs (cycle, reports, reconciliation). |
| 013 | `013-bulk-import` | BeneficiaryManagement | new | CSV mass enrollment with the same CPF rules as feature 002. |
| 014 | `014-notification-service` | (new context, see rejected hypothesis E) | new | Out-of-scope today — would need to justify a new bounded context. Park unless SENARC asks for it. |

## Cross-cutting work (not feature branches)

These ship via the stage branches (`implementacao`, `qualidade`, `operacoes`), not feature branches:

- **Terraform**: AKS + PostgreSQL Flexible Server + Key Vault + App Insights — Pair 5.
- **CI/CD**: workflows for build, ArchUnit gate, OWASP ZAP, equivalence gate — Pair 5.
- **OAuth/Entra ID**: app registration + role mapping (`OPR`, `ADM`, `AUD`, `CON`) — Pair 5.
- **Frontend Next.js 15**: portal for operators (cycles dashboard) and admin (programs catalog) — Pair 4 + Pair 5.

## How to start a new feature

1. `git checkout develop && git pull`
2. `specify` (Spec-Kit will create branch `NNN-feature-name` and check it out)
3. Run `/speckit.specify`, `/speckit.clarify`, `/speckit.plan`, `/speckit.tasks`, `/speckit.analyze`, `/speckit.implement` in sequence
4. Open PR into `develop` once tasks are green
5. Update this file's Active table when the feature branch is created

## Priority signal

Each entry's priority reflects the constitution + business rules catalog, not engineering taste:

- **P1**: feature blocks the critical path (payment cycle running end-to-end) or carries an inviolable security/compliance rule.
- **P2**: feature is required for legal/regulatory parity with the legacy but the cycle can run without it for the workshop demo.
- **P3**: nice-to-have, frontend-heavy, or genuinely new functionality.
