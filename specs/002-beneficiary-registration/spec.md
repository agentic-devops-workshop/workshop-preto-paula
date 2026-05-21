# Feature Specification: Beneficiary Registration

**Feature Branch**: `002-beneficiary-registration`

**Created**: 2026-05-20

**Status**: Draft

**Bounded Context**: `BeneficiaryManagement` (see [`bounded-contexts.md`](../../02-spec-moderna/baseline/bounded-contexts.md) §1)

**Input**: User description: "Beneficiary Registration — BeneficiaryManagement bounded context. Replaces legacy CADBENEF + CADDEPEND + VALBENEF + VALDOCS with a single Spring Boot module that handles enrollment, dependent management, document validation, and lifecycle for the 4.2M-beneficiary base. Must preserve all legacy business rules while resolving findings BONUS-01 (status `S` semantic conflict), BONUS-03 (CPF mask leak), MYS-001 (silent senior status), MYS-002 (dependent count mismatch)."

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Register a New Beneficiary (Priority: P1)

An authorized operator (role `ADM` or `OPR`) registers a new beneficiary by entering CPF, name, birth date, sex, address, NIS, and the social program code. The system validates every field, persists the record with audit, and confirms creation.

**Why this priority**: this is the core entry point for every other operation in the system. Without it, no payment can be calculated, no eligibility checked, no report generated. It also enforces the LGPD-compliant CPF mask and the strict Mod-11 validation that replaces the legacy backdoors in production.

**Independent Test**: Register a beneficiary with a valid Mod-11 CPF (e.g., `12345678909`), assert the API returns 201 with the masked CPF (`XXX.XXX.XXX-09`), assert the database contains a row with the full unmasked CPF, assert an `audit_event` row exists with `action = "BENEFICIARY_REGISTERED"`.

**Acceptance Scenarios**:

1. **Given** a valid Mod-11 CPF, name with at least 2 words, birth date in the past, sex `M`/`F`, and an active social program, **When** operator posts the registration, **Then** the system creates the beneficiary with status `Active`, returns 201 with masked CPF, and writes an audit event.
2. **Given** a CPF that fails Mod-11 validation, **When** operator posts the registration, **Then** the system returns 400 with `error: CPF_INVALID` and does not write to the database.
3. **Given** a CPF that already exists in `Active` status, **When** operator attempts re-registration, **Then** the system returns 409 with `error: BENEFICIARY_ALREADY_EXISTS` and references the existing record's masked CPF.
4. **Given** a CPF whose prefix is `000`/`001`/`002`/`010`/`011`/`099`/`100`/`999` (legacy backdoor), **When** operator posts in production environment, **Then** the system returns 400 with `error: CPF_INVALID` (backdoors are disabled per CON-009).
5. **Given** a CPF with the legacy backdoor prefix, **When** operator posts in `dev`/`test`/`staging` environment with `sifap.cpf.allowTestCpfs=true`, **Then** the system accepts and writes a `TEST_CPF_ACCEPTED` audit event.

---

### User Story 2 — Add Dependents to a Beneficiary (Priority: P1)

After a beneficiary is registered, the operator adds dependents (max configurable per program, default 5) with name, parentage, birth date, and optional CPF. The system rejects dependent additions when the titular is in status `Cancelled` or `Disabled`.

**Why this priority**: dependents directly affect benefit calculation (the family factor uses dependent count). Without dependent management, the calculation in PaymentProcessing produces wrong values. P1 because financial impact.

**Independent Test**: Register a beneficiary, then post 3 dependents one by one. Assert each returns 201 with the dependent payload. Then post a 4th to a beneficiary set to `Cancelled` and assert 409. Verify the `dependent` child table has 3 rows linked by FK to the beneficiary.

**Acceptance Scenarios**:

1. **Given** an active beneficiary with 0 dependents, **When** operator adds a dependent with valid data, **Then** the system creates the dependent in the `dependent` child table and returns 201.
2. **Given** an active beneficiary with `max_dependents` already reached (default 5), **When** operator attempts another, **Then** the system returns 409 with `error: MAX_DEPENDENTS_REACHED`.
3. **Given** a beneficiary in `Cancelled` or `Disabled` status, **When** operator attempts to add a dependent, **Then** the system returns 409 with `error: TITULAR_INACTIVE`.
4. **Given** an attempt to add a dependent with a CPF already present as another dependent on the same beneficiary (and CPF ≠ 0), **When** posted, **Then** the system returns 409 with `error: DEPENDENT_CPF_DUPLICATED`.
5. **Given** a dependent without a CPF (CPF = 0 / null), **When** posted, **Then** the system creates it without enforcing CPF uniqueness — supporting children without CPF (BR-009).

---

### User Story 3 — Query and Display Beneficiary with Masked CPF (Priority: P2)

An authorized user (any of `ADM`, `OPR`, `CON`, `AUD`) searches a beneficiary by CPF or NIS and views the record on screen. All CPF displays use the LGPD-compliant mask `XXX.XXX.XXX-NN`. Full CPF is shown only via the explicit privileged "Reveal" action.

**Why this priority**: every other workflow depends on finding the right beneficiary. The mask correction is also the primary remediation for BONUS-03 and BONUS-04 (legacy mask leaks). P2 because read-only consultations don't block payment, but they block operations.

**Independent Test**: Register a beneficiary with CPF `12345678909`. Query via `GET /api/v1/beneficiaries/12345678909`. Assert response JSON contains `"cpf": "XXX.XXX.XXX-09"` and never the full digit string. Call `POST /api/v1/beneficiaries/12345678909/reveal-cpf` with `purpose` field. Assert response contains full CPF AND an audit event `CPF_FULL_VIEW` was created.

**Acceptance Scenarios**:

1. **Given** a registered beneficiary, **When** any role queries by CPF, **Then** the system returns the record with all CPF fields masked as `XXX.XXX.XXX-NN`.
2. **Given** a search by NIS, **When** an operator posts the NIS, **Then** the system returns the corresponding beneficiary with masked CPF.
3. **Given** a privileged user (`ADM` or `AUD`) needs the full CPF for an audit case, **When** they call the reveal endpoint with a non-empty `purpose`, **Then** the system returns the full CPF AND creates an `audit_event` row with the purpose, user, and timestamp.
4. **Given** an attempt to call the reveal endpoint without `purpose` (empty or missing), **When** posted, **Then** the system returns 400 and no audit event is created.
5. **Given** a log line that tries to log `"CPF: 12345678909"`, **When** captured by the log filter, **Then** the persisted log entry shows `"CPF: XXX.XXX.XXX-09"` — never the raw digits.

---

### User Story 4 — Update Beneficiary Lifecycle Status (Priority: P2)

An authorized operator changes a beneficiary's status (Active → Suspended, Suspended → Active, Active → Cancelled, etc.) with a mandatory reason. The system records the transition in the audit trail. The status `S` from the legacy is resolved into two explicit concepts: `LifecycleStatus` (Active/Suspended/Cancelled/Inactive/Disabled) and `AgeCategory` (Adult/Senior/Minor) — they are independent.

**Why this priority**: the legacy mixed "Senior" and "Suspended" into a single code `S`, causing BONUS-01 and MYS-001. Untangling them is critical for correctness but is secondary to the primary registration flow.

**Independent Test**: Register a beneficiary. Call `PATCH /api/v1/beneficiaries/{cpf}/status` with `lifecycleStatus: Suspended` and a reason. Assert the response shows the new status, the beneficiary record is updated, and an audit event with `action: BENEFICIARY_STATUS_CHANGED`, `before: Active`, `after: Suspended` is created. Separately verify that `ageCategory` is computed from birth date and never written by the operator.

**Acceptance Scenarios**:

1. **Given** an active beneficiary, **When** operator changes lifecycle status to `Suspended` with a reason, **Then** the system updates the record and writes an audit event with both before/after states.
2. **Given** a status change without a reason (empty or missing), **When** posted, **Then** the system returns 400 with `error: REASON_REQUIRED`.
3. **Given** a beneficiary turning 76 (legacy threshold for senior was 75), **When** the daily birthday job runs, **Then** the system updates `ageCategory` to `Senior` and creates an audit event `AGE_CATEGORY_CHANGED` — without affecting `lifecycleStatus`.
4. **Given** a forbidden transition (e.g., `Cancelled → Active`), **When** operator attempts it, **Then** the system returns 409 with `error: INVALID_STATUS_TRANSITION` listing the allowed transitions.

---

### User Story 5 — Update Beneficiary Personal Data (Priority: P3)

An authorized operator updates non-immutable beneficiary fields (address, phone, RG). Immutable fields (CPF, birth date, sex, program code, registration date) are rejected by the API even if present in the payload.

**Why this priority**: operational convenience for routine corrections (address changes, phone updates). Lower priority because incorrect address doesn't block payment generation.

**Independent Test**: Register a beneficiary. PATCH with a new address. Assert the response shows the new address and an audit event was created. Then attempt to PATCH with a new CPF. Assert 409 with `error: IMMUTABLE_FIELD`.

**Acceptance Scenarios**:

1. **Given** an active beneficiary, **When** operator patches the address fields, **Then** the system updates them and writes an audit event with before/after state.
2. **Given** a patch payload containing `cpf` or `birthDate` or `sex` or `programCode`, **When** posted, **Then** the system returns 409 with `error: IMMUTABLE_FIELD` listing which fields are immutable.
3. **Given** a patch on a `Cancelled` beneficiary, **When** posted, **Then** the system returns 409 with `error: BENEFICIARY_NOT_ACTIVE`.

---

### Edge Cases

- **What if** the operator submits a CPF that is mathematically valid (passes Mod-11) but contains a known legacy backdoor pattern (e.g., all zeros: `00000000000`)? → In production, the strict CPF validation rejects it because all-equal-digits CPFs are not real, even when they pass Mod-11. In test environments, the backdoor flag permits it but an audit event flags the acceptance.
- **What if** a beneficiary has `birthDate` in the future (data-entry error)? → Reject with 400 `error: INVALID_BIRTH_DATE` — birth date must be ≤ current date.
- **What if** a beneficiary's `nis` is already in use by another active beneficiary? → Return 409 `error: NIS_ALREADY_USED` referencing the masked CPF of the existing holder.
- **What if** the operator searches by a CPF that was previously associated with a `Cancelled` beneficiary, and that record was kept for audit (soft delete)? → Return the historical record with `status: Cancelled` and `cancelledAt` timestamp — never a 404.
- **What if** the migration from Adabas brought a beneficiary record with one of the legacy backdoor CPFs (e.g., `99999999999`)? → The record is readable with `cpf_validation_status = LEGACY_BACKDOOR` but cannot be edited unless the CPF is corrected to a Mod-11-valid value (see ADR-008).
- **What if** the operator's session token expires mid-operation? → Return 401 `error: TOKEN_EXPIRED`; the operation is not partially applied.

## Requirements *(mandatory)*

### Functional Requirements

> Every FR here maps to one or more functional requirements in [`02-spec-moderna/baseline/requirements.md`](../../02-spec-moderna/baseline/requirements.md) §2.1 and to one or more legacy business rules in [`01-arqueologia/output-requisitos/business-rules-catalog.final.md`](../../01-arqueologia/output-requisitos/business-rules-catalog.final.md).

| ID | Requirement | source_legacy |
|---|---|---|
| FR-001 | System MUST allow registration of beneficiaries with CPF, name, birth date, sex, address, NIS, and the social program code. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L60-L94` |
| FR-002 | System MUST validate CPF using the Mod-11 algorithm (strict mode in production). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L227-L271` + `VALBENEF.NSN#L131-L210` |
| FR-003 | System MUST reject CPFs whose prefix matches the legacy backdoors (`000`/`001`/`002`/`010`/`011`/`099`/`100`/`999`) in production environment. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L160-L175` (legacy bypass — now disabled per ADR-008) |
| FR-004 | System MUST accept CPFs with legacy backdoor prefixes ONLY in non-production environments where `sifap.cpf.allowTestCpfs=true`, and MUST log every acceptance as an audit event. | `[GREENFIELD]` (test ergonomics — replaces the legacy bypass that was active in production) |
| FR-005 | System MUST refuse to start in production if `sifap.cpf.allowTestCpfs=true` (boot-time guard). | `[GREENFIELD]` (operational safety, per ADR-008) |
| FR-006 | System MUST require name to contain at least two whitespace-separated tokens (first name + last name). | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L279-L296` |
| FR-007 | System MUST validate UF against the 27 official Brazilian UF codes. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L19-L46` |
| FR-008 | System MUST persist the full CPF in the database while masking it as `XXX.XXX.XXX-NN` in every API response, log line, and UI display. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L191-L208` (legacy mask was inconsistent — now uniform per ADR-007) + `RELPGT.NSN#L113-L115` |
| FR-009 | System MUST expose a dedicated reveal endpoint that returns the full CPF to authorized roles (`ADM`, `AUD`) with a mandatory `purpose` field, and MUST write an audit event recording the access. | `[GREENFIELD]` (replaces the legacy implicit full-CPF exposure with explicit, audited access per ADR-007) |
| FR-010 | System MUST add dependents as rows in a `dependent` child table linked by FK to `beneficiary`, with name, parentage, birth date, optional CPF, and parentage type. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L99-L110` (legacy PE group — now relational per ADR-002) |
| FR-011 | System MUST limit dependents per beneficiary to a configurable maximum (default 5) read from `social_program.max_dependents` per program. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L57-L59` (legacy hardcoded 5 — now configurable) + `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm` (DDM allowed 10 — see MYS-002) |
| FR-012 | System MUST reject dependent addition when the titular beneficiary is in `Cancelled` or `Disabled` status. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L50-L53` |
| FR-013 | System MUST allow a dependent without CPF (CPF = 0 or null) and MUST NOT enforce CPF uniqueness in that case. | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L87-L94` |
| FR-014 | System MUST model the beneficiary's lifecycle and age as TWO independent concepts: `LifecycleStatus` (Active/Suspended/Cancelled/Inactive/Disabled) and `AgeCategory` (Adult/Senior/Minor). | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L155-L157` (legacy conflated both into status `S` — see BONUS-01) |
| FR-015 | System MUST compute `AgeCategory` from birth date using full date arithmetic (year + month + day), not year-only as the legacy did. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L146-L149` (legacy used year only — see MYS-001) |
| FR-016 | System MUST run a daily job that recomputes `AgeCategory` for beneficiaries crossing the senior threshold (age 75 by default) and emits `AGE_CATEGORY_CHANGED` audit events. | `[GREENFIELD]` (legacy silently mutated status — now explicit and audited per MYS-001 remediation) |
| FR-017 | System MUST enforce that the following fields are immutable after initial registration: CPF, birth date, sex, original program code, registration date. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L200-L218` (implicit in legacy update logic) |
| FR-018 | System MUST allow updates to the following fields on active beneficiaries: name, address, phone, RG, NIS. | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L200-L218` |
| FR-019 | System MUST allow lifecycle status transitions with a mandatory non-empty `reason` field on every change. | `[GREENFIELD]` (legacy allowed silent status changes — see MYS-001) |
| FR-020 | System MUST enforce valid lifecycle status transitions per a documented state machine and reject forbidden transitions with HTTP 409. | `[GREENFIELD]` (legacy had no explicit state machine) |
| FR-021 | System MUST search beneficiaries by CPF or NIS via dedicated endpoints. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L85-L98` |
| FR-022 | System MUST return a beneficiary's payment history (last N months, paginated) via a query endpoint, with masked CPF and the standard pagination contract. | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L162-L172` |
| FR-023 | System MUST publish domain events `BeneficiaryRegistered`, `BeneficiaryStatusChanged`, `DependentAdded`, `DependentRemoved`, `AgeCategoryChanged` for cross-context consumption (especially by `ReportingAndAudit`). | `[GREENFIELD]` (replaces the legacy implicit cross-program coupling — per ADR-001) |
| FR-024 | System MUST write an `audit_event` row for every create, update, delete (logical), and privileged read operation on a beneficiary. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L218-L255` (audit pattern from legacy) |
| FR-025 | System MUST mark Adabas-migrated records carrying legacy-backdoor CPFs with `cpfValidationStatus = LEGACY_BACKDOOR` and make them readable but not editable until the CPF is corrected. | `[GREENFIELD]` (transition mechanism for migrated data per ADR-006 and ADR-008) |

### Key Entities

- **Beneficiary**: the citizen receiving a social benefit. Attributes: CPF (immutable, unique), NIS, name, birthDate, sex, address (logradouro, número, complemento, bairro, município, UF, CEP), phone, RG, lifecycleStatus, ageCategory, cpfValidationStatus, programCode (immutable, FK to SocialProgram), registrationDate (immutable), createdBy, updatedBy, sourceLegacyIsn (nullable, for migrated records).
- **Dependent**: a family member linked to a Beneficiary. Attributes: parentBeneficiaryId (FK), name, birthDate, parentage (Spouse/Child/Sibling/Other), cpf (nullable), document, sex. Maximum N per parent (configurable per program).
- **AuditEvent**: an immutable record of any change. Attributes: id, timestamp, actorUserId, actorRole, action (BENEFICIARY_REGISTERED, BENEFICIARY_STATUS_CHANGED, DEPENDENT_ADDED, ...), entityType, entityId, beforeState (JSONB), afterState (JSONB), reason, correlationId, ipAddress.
- **CpfValidationStatus**: enum { VALID, LEGACY_BACKDOOR, TEST } indicating how the CPF was accepted at registration.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Operator completes a new beneficiary registration (single titular, no dependents) end-to-end in ≤ 90 seconds, including all validation feedback (NFR-USAB-003).
- **SC-002**: API endpoint `POST /api/v1/beneficiaries` sustains 100 requests/second with P95 latency ≤ 300 ms on a production-equivalent instance (NFR-PERF-001).
- **SC-003**: Beneficiary search by CPF returns results with P95 ≤ 200 ms across the 4.2M-row population (NFR-PERF-005).
- **SC-004**: 100% of CPF displays in any API response, log line, or UI render use the standard mask `XXX.XXX.XXX-NN`. Validated by an automated CI scan that fails the build if any 11-digit numeric sequence is found in test-run logs (NFR-SEC-001).
- **SC-005**: 100% of state-changing operations write at least one `audit_event` row (NFR-COMP-002).
- **SC-006**: Migration from the Adabas snapshot produces beneficiary records whose count matches the source within tolerance ±0 (every legacy record either lands in `beneficiary` or is explicitly logged as rejected — no silent loss) (ADR-006 validation).
- **SC-007**: Application refuses to start in production when `sifap.cpf.allowTestCpfs=true` — verified by an integration test that boots the production profile (NFR-SEC-004).
- **SC-008**: Equivalence test passes on 10,000 randomized registrations — same input through the new system and a faithful Java port of the legacy validation produces identical accept/reject decisions in `LEGACY_TRUNCATE` mode (NFR-MAIN-001 + ADR-003).

## Assumptions

- **Authentication is in place**: the API consumers carry a valid OAuth 2.0/JWT issued by Azure Entra ID; this spec assumes FR-AUTH-001 is implemented by an earlier or parallel feature.
- **Social Program catalog exists**: a beneficiary cannot be registered for a program that is not in the `social_program` table; this spec depends on `SocialProgramRegistry` being deployed first.
- **PostgreSQL schema is provisioned**: tables `beneficiary`, `dependent`, `audit_event`, `social_program` exist with the agreed columns, including `source_legacy_isn` per ADR-006 and `cpf_validation_status` per ADR-008.
- **Migration pipeline runs separately**: this spec does not implement the Strangler Fig migration of 4.2M records — that is covered by ADR-006 and a separate operational runbook.
- **Email/SMS notifications are out of scope** for this spec (no `Notification` bounded context yet — see `bounded-contexts.md` rejected hypothesis E).
- **Default `max_dependents=5`** is the operational default until SENARC confirms otherwise (open question in `requirements.md` §6.1 — see MYS-002).
- **Brazilian PT-BR locale** for all user-facing strings; this spec does not address other locales (OUT-005).
- **The Beneficiary read API is paginated** with cursor-based pagination of 50 items per page by default; consumers must follow the cursor pattern (FR-API-001 standard).

## Dependencies

- `SocialProgramRegistry` bounded context (read-only `findActiveByCode`)
- `ReportingAndAudit` bounded context (consumer of all events emitted here)
- Cross-cutting: OAuth/JWT auth, Application Insights, Azure Key Vault, Spring Security

## Constraints

- CON-001: stack-alvo (Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Azure)
- CON-002: Modular Monolith (this feature lives in package `com.sifap.beneficiary`)
- CON-003: every requirement carries `source_legacy` (verified above)
- CON-008: LGPD compliance — masking + audit + right-to-deletion endpoint
- CON-009: CPF backdoors disabled in production
- CON-010: payments uniqueness — does not apply directly to registration but reminds us not to create duplicate beneficiary records that would violate downstream

## Applicable ADRs

- [ADR-001](../../02-spec-moderna/baseline/ADR-001-modular-monolith.md) — package boundaries
- [ADR-002](../../02-spec-moderna/baseline/ADR-002-pe-groups-mapping.md) — dependents as child table
- [ADR-006](../../02-spec-moderna/baseline/ADR-006-data-migration-strategy.md) — `source_legacy_isn` column, validation status for migrated rows
- [ADR-007](../../02-spec-moderna/baseline/ADR-007-pii-masking-policy.md) — CPF masking pattern, reveal endpoint
- [ADR-008](../../02-spec-moderna/baseline/ADR-008-cpf-test-backdoors.md) — feature flag, boot guard

## Open Questions

> Items requiring `/speckit.clarify` or SENARC confirmation before implementation.

1. **[NEEDS-CLARIFICATION]** Final value of `max_dependents` default — SENARC validation of 3 vs 5 vs 10 (open per `requirements.md` §6.1, MYS-002).
2. **[NEEDS-CLARIFICATION]** Final lifecycle state machine — confirm allowed transitions (e.g., is `Cancelled → Active` ever permitted via a reinstatement workflow?).
3. **[NEEDS-CLARIFICATION]** Daily age-category job execution time and timezone (suggested: 02:00 America/Sao_Paulo).
4. **[NEEDS-CLARIFICATION]** Soft-delete vs hard-delete on `DELETE /api/v1/beneficiaries/{cpf}`: presumed soft (lifecycle → `Cancelled` with retention per LGPD), pending PO confirmation.

## Out of Scope

Items explicitly NOT covered by this spec:

- Payment calculation (covered by `PaymentProcessing` bounded context, separate spec)
- Eligibility validation across programs (covered by `SocialProgramRegistry`, separate spec)
- Report generation (covered by `ReportingAndAudit`, separate spec)
- Notifications to beneficiaries (out of scope per OUT-006)
- Bulk import via CSV (could be a separate feature later)
- Public self-service registration by beneficiary (admin-only system per OUT-006)
