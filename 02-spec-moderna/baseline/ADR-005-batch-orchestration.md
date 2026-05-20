<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-005: Batch Orchestration — Spring Batch + Quartz Inside the Monolith

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-00A4EF?style=for-the-badge) ![STATUS Accepted](https://img.shields.io/badge/STATUS-Accepted-7FBA00?style=for-the-badge) ![SEVERITY High](https://img.shields.io/badge/SEVERITY-High-FFB900?style=for-the-badge)

## Status

**Accepted** — 2026-05-20

## Deciders

- Par 2 · Architecture
- Reviewed by: Par 3 · Implementation (Tech Lead) — owns batch implementation
- Reviewed by: Par 5 · Operations (DevOps) — owns scheduling infrastructure

## Technical Story

The legacy SIFAP runs three batch jobs orchestrated by z/OS JES2:

| Legacy program | Frequency | Volume | Criticality |
|---|---|---|---|
| `BATCHPGT.NSN` | Monthly (1st business day) | 3.8M payments generated | CRITICAL (financial) |
| `BATCHCON.NSN` | Daily during conciliation window | ~120K CNAB 240 records | CRITICAL (financial) |
| `BATCHREL.NSN` | Monthly + on-demand | 3.8M records summarized | HIGH (regulatory) |
| `CALCCORR.NSN` | Quarterly / on-demand | Variable (retroactive) | HIGH (financial) |
| `RELAUDIT.NSN`, `RELPGT.NSN` | On-demand | Variable | MEDIUM |

JES2 provides:

- Cron-like scheduling
- Job dependencies (Job B starts only when Job A succeeds)
- Restart/checkpoint at last successful step
- Centralized job log
- Operator console for manual intervention

We need an equivalent in the Java + Azure stack that handles 4.2M records in a ≤ 4-hour window (NFR-PERF-003) and supports partial reprocessing (NFR-SCAL-003).

## Context and Problem Statement

We must orchestrate scheduled and on-demand batch processing in SIFAP 2.0 with these requirements:

1. **Reliable monthly cycle**: payment generation must complete in ≤ 4 hours on the 1st business day
2. **Partial reprocessing**: if the job fails at beneficiary #2.5M of 4.2M, restart should resume from the last commit point — not restart from scratch
3. **Daily conciliation**: triggered after the bank returns the CNAB file (variable timing)
4. **On-demand reports**: operators trigger from admin UI
5. **Observability**: each job run must be queryable (status, duration, records processed, errors)
6. **Operational simplicity**: must fit the modular monolith deployment (ADR-001)

## Decision Drivers

- **D1 — Volume**: 4.2M records in 4 hours = ~290 records/sec sustained
- **D2 — Reliability**: financial batch — failure is not tolerated; partial reprocessing required
- **D3 — Deployment model**: monolith (ADR-001) → prefer in-process orchestration over external services
- **D4 — Operational maturity**: workshop team has no SRE for managing a separate scheduler service
- **D5 — Cloud portability**: prefer cloud-agnostic tools (NFR-PORT-001)
- **D6 — Observability**: must integrate with Application Insights (NFR-OBS-003)
- **D7 — Cost**: Azure Functions Premium plan with always-on instance is expensive at low utilization

## Considered Options

1. **Spring `@Scheduled` annotations** — simplest, in-process, cron-based
2. **Azure Functions (Timer Trigger)** — serverless, separate from the monolith
3. **Spring Batch + Quartz** — full batch framework with state, chunking, restart
4. **Standalone job runner image** (Spring Boot CLI invoked via Kubernetes Jobs or Azure Container Apps Jobs)
5. **Apache Airflow** — separate orchestrator with DAGs

## Decision

**Chosen option: 3 — Spring Batch + Quartz inside the monolith.**

- **Spring Batch** provides:
  - Chunk-oriented processing: read N records, process them, commit, repeat
  - `JobRepository` table tracks every job execution with status, start/end time, parameters
  - Automatic restart from last commit point on failure
  - Step composition (e.g., `BATCHCON` = step 1 read CNAB → step 2 reconcile → step 3 audit)
  - Built-in metrics for Application Insights via Micrometer
- **Quartz Scheduler** provides:
  - Cron expressions for scheduled jobs (`0 0 2 1W * ?` = 02:00 on 1st business day)
  - Cluster-mode for multi-instance monolith (job runs once even with N replicas)
  - Persistent job store in PostgreSQL (survives restart)
  - Manual trigger from admin UI for on-demand jobs
- **Same JVM as the monolith**: no separate deploy, no service discovery, shares the same `BeneficiaryRepository`/`PaymentRepository` beans → ADR-001 alignment
- **Optional isolation**: heavy batch jobs run on a dedicated app instance (same image, different replica with `spring.profiles.active=batch`) → web traffic and batch don't compete for resources

### Job catalog

| Job name | Class | Trigger | Volume | Chunk size |
|---|---|---|---|---|
| `monthlyPaymentGeneration` | `BatchPaymentJob` | Quartz `0 0 2 1W * ?` | 4.2M | 500 |
| `dailyBankReconciliation` | `BatchConciliationJob` | API trigger (CNAB upload) | ~120K | 1000 |
| `monthlyConsolidatedReport` | `BatchReportJob` | Quartz `0 0 6 1W * ?` | 3.8M | aggregate |
| `retroactiveCorrection` | `BatchCorrectionJob` | API trigger (operator) | Variable | 200 |

## Pros and Cons of the Options

### Option 1 — Spring `@Scheduled`

- ✅ Zero extra dependencies
- ✅ Familiar to every Spring developer
- ❌ **No restart from failure**: if the method throws at record 2.5M, the next run starts from scratch
- ❌ **No checkpointing**: cannot resume; no progress tracking
- ❌ **Not cluster-safe**: with N replicas, the job runs N times (need to add custom DB lock — reinvents Quartz)
- ❌ Insufficient for 4.2M records — can't meet NFR-PERF-003

### Option 2 — Azure Functions (Timer Trigger)

- ✅ Serverless: no infra to manage
- ✅ Auto-scale per execution
- ❌ **10-minute execution limit on Consumption plan**: 4.2M records in 10 min = 7000 rec/sec — impossible with DB round-trips
- ❌ Premium plan needed for long executions → cost spikes, defeats serverless benefit
- ❌ **Separate deployment artifact**: contradicts ADR-001 (single monolith)
- ❌ **Cold start risk** for daily jobs
- ❌ State management still requires external DB (no Spring Batch JobRepository)

### Option 3 — Spring Batch + Quartz (chosen)

- ✅ Designed exactly for this workload: chunk processing, restart, JobRepository
- ✅ Battle-tested in financial-services contexts (banks, insurance)
- ✅ Stays in the monolith → ADR-001 alignment, no extra image
- ✅ Cluster-safe (Quartz with PostgreSQL job store handles multi-instance correctly)
- ✅ Restart from last commit: 290 records/sec × 4h works because failures don't reset progress
- ✅ Native Micrometer metrics → Application Insights dashboards
- ✅ Cloud-portable (no Azure-specific APIs)
- ❌ **Learning curve**: Spring Batch concepts (Step, ItemReader, ItemProcessor, ItemWriter, JobLauncher) require team ramp-up
- ❌ Adds tables to schema (`BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `QRTZ_*`) — minor schema bloat
- ❌ Heavy jobs on the same instance as the web tier can cause GC pressure → mitigated by dedicated batch profile

### Option 4 — Standalone Spring Boot CLI job runner

- ✅ Job runs isolated from web tier (no resource competition)
- ❌ **Requires separate image, CI/CD pipeline, secrets, observability config** — operational cost
- ❌ Loses the JVM warm-up benefit of long-running monolith
- ❌ Cold start on each run (~30s) — significant for sub-minute jobs

### Option 5 — Apache Airflow

- ✅ Excellent for complex DAGs with cross-system dependencies
- ❌ **Massive overkill** for 4 jobs with simple dependencies
- ❌ Requires running Airflow infra (scheduler, workers, web UI, metadata DB)
- ❌ Python-based — adds a second language to a Java monolith
- ❌ Team has no Airflow expertise

## Consequences

### Positive

- 4.2M-record batch completes reliably with checkpointing — restart from failure is automatic
- Single deployment unit preserved (ADR-001) — one Docker image
- Operator can trigger jobs from admin UI via REST endpoint that calls `JobLauncher.run(job, params)`
- Job history queryable via standard SQL on `BATCH_JOB_EXECUTION` table → audit-ready
- Cluster-safe by Quartz design → no manual locking
- Metrics out-of-the-box via Spring Boot Actuator + Micrometer → Application Insights dashboards

### Negative

- **Schema bloat**: ~12 new tables for Spring Batch + Quartz metadata. Mitigation: separate Flyway folder `db/migration/batch/`, kept apart from business schema
- **Memory pressure during batch**: 500-item chunks × parallel steps can pressure heap. Mitigation: separate `batch` profile instance with higher `-Xmx`
- **Quartz misfire policy** must be tuned (what if the job's scheduled time arrives but a previous instance is still running?). Mitigation: explicit `MISFIRE_INSTRUCTION_DO_NOTHING` for monthly jobs (we don't want to fire twice)
- **Operator learning curve**: Spring Batch admin requires understanding of Job vs JobInstance vs JobExecution
- **JobRepository tables are NOT subject to LGPD-style retention rules** — they accumulate forever unless we add cleanup. Mitigation: monthly retention job that purges `BATCH_JOB_EXECUTION` rows older than 1 year

### Neutral

- Total job runtime vs legacy: similar (~4 hours). The Java implementation gains parallelism via Spring Batch partitioning if needed.

## Validation

- ✅ **Restart test**: kill the batch process at random record (e.g., 2,123,456 of 4.2M) → restart → completes correctly, skips already-committed records, no duplicates
- ✅ **Idempotency test**: rerun completed job with same parameters → no duplicate payments (validated by FR-PAY-013 unique constraint)
- ✅ **Performance test**: seed 4.2M test beneficiaries → run `monthlyPaymentGeneration` → completes in ≤ 4h on production-equivalent instance
- ✅ **Cluster test**: deploy 3 monolith instances → Quartz fires once across the cluster (validated by `BATCH_JOB_EXECUTION.HOST_NAME` showing a single instance executed)
- ✅ **Observability test**: triggered batch shows up in Application Insights as a custom event with `JobName`, `Status`, `Duration`, `RecordsProcessed` properties
- ✅ **Operator UX test**: admin UI shows running job progress (records processed / total), allows pause/resume/abort
- ✅ **Cleanup test**: retention job purges `BATCH_JOB_EXECUTION` rows > 1 year old — does NOT touch business `payment` table

## Related Requirements

- FR-PAY-012: Monthly payment batch for all active beneficiaries
- FR-PAY-014: Preserve CPF ordering for downstream compatibility (Spring Batch partition by hash of CPF or ORDER BY)
- FR-PAY-015: CNAB reconciliation
- FR-PAY-017: Retroactive correction (CALCCORR equivalent)
- FR-PAY-019: Audit event for conciliation
- NFR-PERF-003: Batch completes in ≤ 4 hours
- NFR-SCAL-003: Partial reprocessing supported
- NFR-OBS-003: RED metrics exposed
- CON-006: Audit trail immutable (Spring Batch metadata is operational, separate from business audit)

## References

- [`techstack.md`](techstack.md) — section 6, decision #4
- [`modular-monolith.instructions.md`](../../.github/instructions/modular-monolith.instructions.md)
- [Spring Batch 5.x reference](https://docs.spring.io/spring-batch/reference/index.html)
- [Quartz Scheduler clustering](https://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/ConfigJDBCJobStoreClustering.html)
- Michael T. Nygard, *Release It!* — chapter on batch processing patterns

---
