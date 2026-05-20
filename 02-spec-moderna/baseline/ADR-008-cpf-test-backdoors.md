<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-008: CPF Test Backdoors — Disabled in Production via Feature Flag

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY High](https://img.shields.io/badge/SEVERITY-High-FFB900?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture
- Reviewed by: Par 5 · Operations (Security) — owns production configuration
- Reviewed by: Par 4 · Quality (QA Engineer) — needs test fixtures
- **Notify**: Security/InfoSec team for explicit sign-off on the feature flag boundary

## Technical Story

Stage 1 archaeology uncovered TWO backdoors in CPF validation, both flagged as CRITICAL:

### Backdoor 1: VALDOCS prefix bypass (MYS-007, EGG-002)

`VALDOCS.NSN#L160-L175` — sub-routine `CHECK-DOC-ESPECIAL` accepts any CPF starting with one of these 8 prefixes **without Mod-11 validation**:

```text
000, 001, 002, 010, 011, 099, 100, 999
```

Code comment indicates these are "test government" CPFs.

### Backdoor 2: VALBENEF all-equal-digits bypass (EGG-002)

`VALBENEF.NSN#L209-L216` — accepts any CPF where all 11 digits are identical AND start with `000` as valid, bypassing the standard "all-equal-digits = invalid" rule.

Combined: these backdoors mean CPFs like `00012345678` or `00000000000` may be in production database, accepted by the legacy without any verification of legitimacy. We have no way to know how many real production records use these prefixes.

This is **simultaneously a security vulnerability AND a development necessity**: QA teams need predictable test CPFs that won't accidentally match a real Brazilian citizen.

## Context and Problem Statement

We need a policy for these backdoors in SIFAP 2.0 that:

1. **Eliminates the security risk in production** — no one can register or operate on bogus CPFs
2. **Preserves developer/QA ergonomics** — predictable test CPFs in non-production environments
3. **Handles legacy data**: production database may contain records with these backdoor CPFs from 29 years of operation
4. **Is auditable** — every test-CPF acceptance must leave a trail
5. **Is reversible** — if regulators or auditors demand a different policy, we change one flag

The risk of simply removing the backdoors:

- Existing legacy records with backdoor CPFs would become "orphaned" (cannot be queried by Mod-11-strict validation)
- Operators with year-old open audit cases on these records would lose access

The risk of preserving the backdoors:

- A malicious operator (or compromised account) can register fictitious beneficiaries with `999.999.999-99` and route payments to themselves
- A bug in CPF input could accept obviously invalid data
- A compliance audit would flag this immediately

## Decision Drivers

- **D1 — Security**: production must reject invalid CPFs by default
- **D2 — Test predictability**: dev/test/staging environments need known valid CPFs that won't collide with real people
- **D3 — Legacy data handling**: existing records with backdoor CPFs must remain queryable but not editable
- **D4 — Auditability**: every backdoor usage logged
- **D5 — Reversibility**: change via configuration, not code deploy
- **D6 — Standards compliance**: align with Receita Federal's official "test CPF" guidance (CPFs starting with 000 are reserved)

## Considered Options

1. **Remove entirely** — strict Mod-11 always; legacy records with backdoor CPFs become read-only orphans
2. **Preserve unconditionally** — replicate legacy behavior; backdoors active in production
3. **Feature flag** `sifap.cpf.allowTestCpfs` — defaults `false` in production, `true` in non-prod
4. **Environment-aware** — backdoors hardcoded to environment via Spring profiles
5. **Allowlist per environment** — explicit CPF allowlist managed by ops, separate from validation rules

## Decision

**Chosen option: 3 — Feature flag `sifap.cpf.allowTestCpfs` with explicit production override prevention, complemented by audit logging.**

### Behavior matrix

| Environment | `sifap.cpf.allowTestCpfs` | Behavior |
|---|---|---|
| `dev`, `test`, `staging` | `true` (default) | Legacy backdoor accepted; every acceptance logs `audit_event` with `action = "TEST_CPF_ACCEPTED"` |
| `prod` | `false` (forced; cannot be overridden) | Strict Mod-11 only; backdoor CPFs rejected at registration with HTTP 400 + clear error message |

### Production override prevention

```java
@PostConstruct
public void enforceProductionPolicy() {
  if ("prod".equals(activeProfile) && allowTestCpfs) {
    throw new IllegalStateException(
      "FATAL: sifap.cpf.allowTestCpfs MUST be false in production. " +
      "See ADR-008. Refusing to start."
    );
  }
}
```

The application **refuses to start** if any operator tries to enable the backdoor in production. Triple-guarded: environment variable, application.yml, Key Vault.

### Legacy data handling

Records with backdoor CPFs already in the migrated database (from Adabas):

- **Readable**: queries return them normally
- **Marked**: `beneficiary.cpf_validation_status` column = `LEGACY_BACKDOOR` so reports can flag them
- **Not editable** via API: any UPDATE attempt that doesn't change CPF passes; any UPDATE attempt that changes CPF is rejected unless the new CPF passes strict Mod-11
- **Cleanup workflow**: separate admin endpoint `POST /api/v1/admin/legacy-cpf-cleanup/{id}` allows compliance team to mark a backdoor record for manual review

### Documentation in error responses

When `prod` rejects a backdoor CPF, the error response includes a reference:

```json
{
  "error": "CPF_INVALID",
  "message": "CPF rejected: does not pass Mod-11 verification.",
  "reference": "https://docs.sifap.gov.br/cpf-validation",
  "supportContact": "if this CPF was previously accepted, file a ticket referencing ADR-008"
}
```

## Pros and Cons of the Options

### Option 1 — Remove entirely

- ✅ Maximum security from day 1
- ❌ **Breaks legacy data access**: 29 years of records with backdoor CPFs become unreachable via standard validation paths
- ❌ **Breaks test fixtures**: every QA test must hand-build a Mod-11-valid CPF — fragile, error-prone, real CPFs accidentally used
- ❌ **No reversal path**: if a regulatory change demands restoring backdoor behavior, requires code change

### Option 2 — Preserve unconditionally

- ❌ **Compliance failure** — auditors will flag immediately
- ❌ **Fraud vector** — bogus beneficiaries possible
- ❌ **Migrates the bug** rather than fixing it
- ❌ Rejected as **unacceptable**

### Option 3 — Feature flag (chosen)

- ✅ Production safe (backdoor disabled, enforced at boot)
- ✅ QA/dev ergonomics preserved
- ✅ Legacy data remains queryable
- ✅ Auditable: every backdoor usage logged
- ✅ Reversible if regulators change requirements (with explicit ops decision)
- ❌ **Two code paths** to maintain (mitigated: same code, branch on `allowTestCpfs`)
- ❌ **Configuration discipline**: someone must NOT set `true` in prod (mitigated by boot-time enforcement)

### Option 4 — Environment-aware (Spring profiles)

- ✅ Implicit and automatic
- ❌ **Profile pollution**: `prod` profile becomes a security-sensitive boundary; one misconfig and backdoors activate
- ❌ Less explicit than a named feature flag
- ❌ Hides the policy from operators (no central place to verify "are backdoors off?")

### Option 5 — Allowlist per environment

- ✅ Maximum flexibility (specific CPFs whitelisted)
- ❌ **Allowlist becomes a configuration nightmare** — drifts between environments, hard to audit
- ❌ Doesn't match the legacy behavior (which uses prefix-based rule, not list-based)

## Consequences

### Positive

- Production security tightened — strict Mod-11 by default
- QA and dev get predictable test CPFs (the same 8 prefixes + all-zero) → test stability
- Every test-CPF use is logged in non-prod environments → behavior visibility
- Legacy data not broken — readable, marked, with a path to cleanup
- Application self-defends against misconfig via boot-time check
- Feature flag is reversible via Key Vault update (no deploy needed) if regulators ever change rules

### Negative

- **Two validation code paths** (strict + backdoor-tolerant) — minor cognitive overhead, justified by clean unit tests per path
- **Migration overhead**: existing backdoor CPFs need the `cpf_validation_status = LEGACY_BACKDOOR` mark, requiring a one-time backfill
- **Operator support load**: first weeks after cutover, ops may receive tickets like "I'm trying to register 999.999.999-99 and it's failing" — needs user education
- **Schema addition**: new column `cpf_validation_status` on `beneficiary` table
- **CI complexity**: tests must verify behavior in both modes

### Neutral

- Performance: negligible (one boolean check on validation path)

## Validation

- ✅ **Production boot test**: with `spring.profiles.active=prod` and `sifap.cpf.allowTestCpfs=true`, application refuses to start with clear error message
- ✅ **Strict validation test**: in `prod` mode, `POST /api/v1/beneficiaries` with CPF `00012345678` returns `400 Bad Request` with documented error message
- ✅ **Permissive validation test**: in `test` mode, the same request succeeds AND creates `audit_event` row with `action = "TEST_CPF_ACCEPTED"`, `cpf_prefix = "000"`
- ✅ **Mod-11 strict test**: in `prod` mode, valid CPF `12345678909` (real Mod-11 valid) is accepted
- ✅ **Legacy readable test**: a beneficiary with `cpf = "99999999999"` and `cpf_validation_status = "LEGACY_BACKDOOR"` is returned by `GET /api/v1/beneficiaries/99999999999`
- ✅ **Legacy not editable test**: attempting to change CPF on a `LEGACY_BACKDOOR` record to another invalid CPF returns 400; changing to a Mod-11-valid CPF succeeds and updates `cpf_validation_status` to `VALID`
- ✅ **Audit alert**: rule in Application Insights triggers if `TEST_CPF_ACCEPTED` ever appears with `environment = "prod"` (defense in depth — should never happen, but alert catches a misconfig that slipped past the boot check)
- ✅ **CI test in both modes**: same test suite runs twice (once with flag on, once off), verifies expected behavior in each

## Related Requirements

- NFR-SEC-004: Backdoors disabled in production, feature flag for non-prod
- FR-BEN-002: CPF validation by Mod-11
- BR-028: Source rule (VALDOCS prefix bypass)
- BR-029: Source rule (VALBENEF all-equal bypass)
- MYS-007, EGG-002: Findings resolved by this ADR
- CON-009: Backdoors disabled in production (constraint)
- ADR-006: Data migration must mark legacy backdoor records with `cpf_validation_status`

## References

- [`techstack.md`](techstack.md) — section 6, decision #7
- [`security.instructions.md`](../../.github/instructions/security.instructions.md)
- [`mysteries-found.final.md`](../../01-arqueologia/output-requisitos/mysteries-found.final.md) — MYS-007, EGG-002
- [`business-rules-catalog.final.md`](../../01-arqueologia/output-requisitos/business-rules-catalog.final.md) — BR-028, BR-029
- Receita Federal — [Sobre o CPF](https://www.gov.br/receitafederal/pt-br/assuntos/orientacao-tributaria/cadastros/cpf) — official validation rules
- OWASP — [Improper Input Validation](https://owasp.org/www-community/Improper_Input_Validation)
- Pattern: [Feature Toggle Boundary](https://martinfowler.com/articles/feature-toggles.html#OpsToggles)

---