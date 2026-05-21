package com.sifap.paymentprocessing.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Aggregate root for a single monthly cycle execution. */
@Entity
@Table(name = "payment_cycle",
       uniqueConstraints = @UniqueConstraint(name = "uq_cycle", columnNames = {"competence", "program_code"}))
public class PaymentCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competence", nullable = false, length = 7)
    private String competence;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private CycleStatus status = CycleStatus.Pending;

    @Column(name = "triggered_by", nullable = false, length = 64)
    private String triggeredBy;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "started_at")   private OffsetDateTime startedAt;
    @Column(name = "completed_at") private OffsetDateTime completedAt;
    @Column(name = "snapshot_at")  private OffsetDateTime snapshotAt;

    @Column(name = "generated_count",       nullable = false) private long generatedCount = 0;
    @Column(name = "skipped_count",         nullable = false) private long skippedCount = 0;
    @Column(name = "error_count",           nullable = false) private long errorCount = 0;
    @Column(name = "region99_bypass_count", nullable = false) private long region99BypassCount = 0;

    @Column(name = "total_gross_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalGrossAmount = BigDecimal.ZERO;

    @Column(name = "force_backfill",  nullable = false) private boolean forceBackfill = false;
    @Column(name = "backfill_reason") private String backfillReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Version @Column(name = "version", nullable = false) private Long version;

    protected PaymentCycle() {}

    public PaymentCycle(Competence competence, String programCode, String triggeredBy,
                        UUID correlationId, boolean forceBackfill, String backfillReason) {
        this.competence      = competence.asText();
        this.programCode     = programCode;
        this.triggeredBy     = triggeredBy;
        this.correlationId   = correlationId;
        this.forceBackfill   = forceBackfill;
        this.backfillReason  = backfillReason;
    }

    public void start() {
        if (status != CycleStatus.Pending) throw new IllegalStateException("Can only start a Pending cycle");
        this.status = CycleStatus.Running;
        this.startedAt = OffsetDateTime.now();
        this.snapshotAt = this.startedAt;
    }

    public void complete(long generated, long skipped, long errors, long region99, BigDecimal totalGross) {
        if (status != CycleStatus.Running) throw new IllegalStateException("Can only complete a Running cycle");
        this.status = CycleStatus.Completed;
        this.completedAt = OffsetDateTime.now();
        this.generatedCount = generated;
        this.skippedCount = skipped;
        this.errorCount = errors;
        this.region99BypassCount = region99;
        this.totalGrossAmount = totalGross;
    }

    public void abort() {
        if (status != CycleStatus.Running) throw new IllegalStateException("Can only abort a Running cycle");
        this.status = CycleStatus.Aborted;
        this.completedAt = OffsetDateTime.now();
    }

    public void markStale() {
        if (status != CycleStatus.Running) return;
        this.status = CycleStatus.Stale;
    }

    public Long getId()                       { return id; }
    public String getCompetence()             { return competence; }
    public String getProgramCode()            { return programCode; }
    public CycleStatus getStatus()            { return status; }
    public String getTriggeredBy()            { return triggeredBy; }
    public UUID getCorrelationId()            { return correlationId; }
    public OffsetDateTime getStartedAt()      { return startedAt; }
    public OffsetDateTime getCompletedAt()    { return completedAt; }
    public OffsetDateTime getSnapshotAt()     { return snapshotAt; }
    public long getGeneratedCount()           { return generatedCount; }
    public long getSkippedCount()             { return skippedCount; }
    public long getErrorCount()               { return errorCount; }
    public long getRegion99BypassCount()      { return region99BypassCount; }
    public BigDecimal getTotalGrossAmount()   { return totalGrossAmount; }
    public boolean isForceBackfill()          { return forceBackfill; }
    public String getBackfillReason()         { return backfillReason; }
}
