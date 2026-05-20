<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-007: PII Masking Policy — Mask Central Groups, Reveal Last Two Digits

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY Critical](https://img.shields.io/badge/SEVERITY-Critical-F25022?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture
- Reviewed by: Par 1 · Vision (Product Owner) — LGPD compliance owner
- Reviewed by: Par 5 · Operations (DevOps + Tech Writer) — affects UI components and operator UX
- **Notify**: Encarregado de Dados (DPO) for formal LGPD review before production

## Technical Story

Stage 1 archaeology identified two LGPD-violating masking patterns in the legacy:

- **BONUS-03**: `CONSBENF.NSN#L191-L208` uses a sub-routine `MASCARA-CPF` that — for CPFs stored with fewer than 11 digits (zero-padded) — exposes the **first 3 digits** instead of masking them. Explicit code comment says `NAO CORRIGIR SEM APROVACAO DA AUDITORIA` (do not fix without audit approval). Known leak, tolerated.
- **BONUS-04**: `RELPGT.NSN#L113-L115` masks as `***.XXX.XXX-XX` — only the first 3 digits hidden, exposing **8 of 11 digits** (including DV). Effectively no protection.

These are documented as **CRITICAL** in the business rules catalog (BR-040, BR-041) and are the second-highest LGPD risk in the entire system.

We need a **single, consistent, conservative masking policy** applied uniformly across:

- API responses (REST JSON)
- UI displays (Next.js components)
- Logs (server + client)
- Reports (PDF, CSV exports)
- Audit event payloads (when CPF is part of event metadata)

## Context and Problem Statement

Brazilian LGPD (Lei 13.709/2018, Art. 6º, Princípio da Necessidade e Princípio da Segurança) requires that personal data exposure be minimized to the necessary level. CPF is **explicitly defined as sensitive personal data** under SENATRAN guidance and BACEN/Pix standards.

Industry-standard CPF masking conventions:

| Mask format | Reveals | Use case | Risk |
|---|---|---|---|
| `XXX.XXX.XXX-XX` (full hide) | nothing | high-security display | low utility (operators need to recognize the record) |
| `XXX.XXX.XXX-NN` (last 2 only) | last 2 + DV | confirmation display | low risk |
| `XXX.XXX.NNN-NN` (last 5) | last 5 | recognition | medium risk |
| `NNN.XXX.XXX-XX` (first 3) | first 3 | what BONUS-03 does | **HIGH RISK** — first 3 often identify region/agency |
| `XXX.NNN.NNN-NN` (middle + last) | 8 of 11 | what BONUS-04 does | **HIGH RISK** — effectively no protection |

We need to pick **one** convention and apply it everywhere. Operators need enough info to recognize records; system must minimize exposure.

## Decision Drivers

- **D1 — LGPD compliance**: minimum necessary exposure
- **D2 — Operator utility**: must be able to confirm "I'm looking at the right beneficiary"
- **D3 — Consistency**: must work in API, UI, logs, reports — no per-screen variation
- **D4 — Implementation simplicity**: one component, one regex, no per-screen logic
- **D5 — Auditability**: who accessed full CPF, and why, must be logged
- **D6 — Replaceability**: if regulators change requirements, we should change one place
- **D7 — Resolves legacy debt**: must eliminate BONUS-03 and BONUS-04 patterns

## Considered Options

1. **Full mask** — `XXX.XXX.XXX-XX`, no digits revealed
2. **Last 2 + DV** — `XXX.XXX.XXX-NN`, last 2 digits revealed
3. **Last 3** — `XXX.XXX.XNN-NN`, last 3 + DV revealed
4. **Per-context policy** — different masks per UI screen (e.g., detail screen shows more, list screen shows less)
5. **Role-based** — operators see more, regular users see less

## Decision

**Chosen option: 2 — Last 2 + DV (`XXX.XXX.XXX-NN`) as the default mask everywhere; full CPF access requires explicit privilege and logs an audit event.**

- **Default mask format**: `XXX.XXX.XXX-NN` where `NN` is the last 2 digits (the DV, "dígito verificador"). Example: a CPF `12345678901` displays as `XXX.XXX.XXX-01`.
- **Storage**: full CPF stays in the database (encrypted at rest via Azure SQL TDE + column-level for sensitive scenarios). Masking happens **only at display boundaries** (REST serialization, UI render, log formatting, report generation).
- **Single source of truth**: utility class `BrazilianCpfMasker` (backend) and React component `<MaskedCpf cpf={value} />` (frontend). Both delegate to a shared regex contract documented in this ADR.
- **Privileged access** (showing full CPF): requires role `ADM` or `AUD`, requires `purpose` parameter explaining why (e.g., "Investigating audit case #1234"), and logs an `audit_event` with `action = "CPF_FULL_VIEW"`.
- **No exceptions in production**: even superusers see masked CPFs in normal lists. Full CPF only via explicit "Reveal" action that requires the `purpose` justification.
- **Log policy**: SLF4J filter rewrites any 11-digit sequence matching the CPF pattern to the masked form before write. Belt-and-suspenders so a careless `log.info("Processing CPF " + cpf)` doesn't leak.
- **Test enforcement**: CI step greps the log output of integration tests for unmasked CPF patterns → fail build if found.

## Pros and Cons of the Options

### Option 1 — Full mask (`XXX.XXX.XXX-XX`)

- ✅ Strongest LGPD posture
- ❌ **Operators can't distinguish records** — every CPF looks identical in a list
- ❌ Defeats the point of showing CPF at all
- ❌ Forces users to click "reveal" on every row → audit log floods

### Option 2 — Last 2 + DV (`XXX.XXX.XXX-NN`, chosen)

- ✅ **Two digits is enough for recognition** — operators tracking a specific case can verify the right record
- ✅ Two digits is **not enough for cross-referencing** with other databases (no useful identifier exposed)
- ✅ Conservative LGPD posture (matches BACEN guidance for transaction confirmations)
- ✅ Industry-standard pattern (same as iFood, Nubank, Mercado Pago show CPF in confirmation flows)
- ✅ Single, simple format — easy to enforce uniformly
- ❌ Slightly less useful than "last 5" for some search-by-CPF use cases (but `/api/v1/beneficiaries?cpf=...` exists for those)

### Option 3 — Last 3 (`XXX.XXX.XNN-NN`)

- ✅ More recognition utility than 2 digits
- ❌ Crosses the threshold where the partial CPF could correlate with other leaked data sets
- ❌ Not a common industry pattern — operators expect "last 2 + DV"

### Option 4 — Per-context policy

- ❌ **Inconsistency by design** — exactly the failure mode of the legacy (BONUS-03 vs BONUS-04 differ per program)
- ❌ Cognitive load: every screen, every API endpoint needs a masking decision
- ❌ Bugs hide easily — "this one screen shows more than expected" goes unnoticed

### Option 5 — Role-based

- ✅ Operators with legitimate need see more
- ❌ **LGPD: role does not justify exposure** by itself; *purpose* does. Role-based reveal flattens to "ADM always sees full" which violates least-privilege.
- ❌ Privileged access without `purpose` justification doesn't create an audit story

## Decision compromise: keep ADM role for **access enablement**, but require explicit per-access reveal action with purpose

→ this is what Option 2's "privileged access" rule enforces.

## Consequences

### Positive

- **Single mask format** across the entire system — no more BONUS-03/04 inconsistency
- **LGPD posture defensible** — minimum necessary exposure, full CPF access logged and justified
- **Operator UX preserved** — last 2 + DV is enough to recognize "yes this is the case I'm working on"
- **Reusable components** (`BrazilianCpfMasker`, `<MaskedCpf>`) ensure consistency by construction
- **Log filter** prevents accidental leaks (`log.info("CPF: " + cpf)` is automatically masked)
- **CI gate** catches violations before merge
- **Audit story complete**: every full-CPF view has who/when/why

### Negative

- **Migration cost**: ~20 places in legacy reports + ~15 UI screens need to use new component
- **Operator behavior change**: users who relied on full CPF in lists must now use the search functionality (might generate support tickets in first weeks)
- **Log filter has performance cost**: regex on every log line. Measured at < 1% overhead in load test; acceptable.
- **`purpose` field UX friction**: every reveal requires text input. Mitigation: dropdown with common reasons + free-text "other".

### Neutral

- Storage doesn't change — CPF still stored full in DB (encrypted at rest)
- API contract: a new `revealedCpf` field can be returned by the "reveal" endpoint, distinct from the default masked `cpf` field

## Validation

- ✅ **Component test (backend)**: `BrazilianCpfMasker.mask("12345678901")` returns `"XXX.XXX.XXX-01"`; `mask(null)` returns `null` safely; `mask("invalid")` throws clear error
- ✅ **Component test (frontend)**: `<MaskedCpf cpf="12345678901" />` renders `XXX.XXX.XXX-01`; `<MaskedCpf cpf={null} />` renders empty without crashing
- ✅ **API contract test**: `GET /api/v1/beneficiaries/12345678901` returns `{"cpf": "XXX.XXX.XXX-01"}` in JSON — never the full CPF unless explicit reveal endpoint
- ✅ **Reveal endpoint test**: `POST /api/v1/beneficiaries/12345678901/reveal-cpf` with body `{"purpose": "audit case 1234"}` returns full CPF AND creates `audit_event` row with `action = "CPF_FULL_VIEW"`, `purpose`, `user_id`
- ✅ **Log filter test**: `log.info("Processing CPF 12345678901")` produces log line `Processing CPF XXX.XXX.XXX-01`
- ✅ **CI grep test**: integration test logs scanned for `\d{11}` pattern; build fails if any found (covers the case where someone bypasses the filter)
- ✅ **UI accessibility test**: `<MaskedCpf>` includes `aria-label="CPF (mascarado), final 01"` for screen readers
- ✅ **Penetration test**: no API endpoint, no log line, no UI screen, no report exposes more than 2 CPF digits without auditing the access

## Related Requirements

- FR-BEN-008: CPF mask policy `XXX.XXX.XXX-**`
- NFR-SEC-001: Sensitive data never in logs unmasked
- NFR-COMP-001: LGPD compliance
- NFR-COMP-004: CPF mask single policy
- BR-040, BR-041: Source business rules (legacy masking violations)
- BONUS-03, BONUS-04: Findings resolved by this ADR

## References

- [`techstack.md`](techstack.md) — section 6, decision #6
- [`security.instructions.md`](../../.github/instructions/security.instructions.md)
- [`mysteries-found.final.md`](../../01-arqueologia/output-requisitos/mysteries-found.final.md) — BONUS-03, BONUS-04
- Lei 13.709/2018 (LGPD) — Art. 6º (princípios), Art. 11 (dados sensíveis)
- BACEN Resolução nº 4.658/2018 — masking standards for financial institutions
- OWASP — [Sensitive Data Exposure](https://owasp.org/Top10/A02_2021-Cryptographic_Failures/)
- iFood, Nubank, Mercado Pago — public masking conventions in their app screens

---