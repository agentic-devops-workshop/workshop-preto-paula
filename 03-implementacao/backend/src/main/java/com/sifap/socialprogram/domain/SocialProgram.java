package com.sifap.socialprogram.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Aggregate root for SocialProgramRegistry.
 *
 * <p>Owns the program identity, base amount and adjustment factor consumed by
 * PaymentCycle (via the published port {@code SocialProgramQueryPort}). The
 * K-factor constant {@code 0.347215} (MYS-003) lives in PaymentCycle, not here.
 */
@Entity
@Table(name = "social_program")
@EntityListeners(AuditingEntityListener.class)
public class SocialProgram {

    /** 27 Brazilian UF codes (BR-031); region 99 is intentionally excluded (FR-012, MYS-008). */
    public static final Set<String> BRAZILIAN_UFS = Set.of(
        "AC","AL","AM","AP","BA","CE","DF","ES","GO","MA","MG","MS","MT","PA",
        "PB","PE","PI","PR","RJ","RN","RO","RR","RS","SC","SE","SP","TO");

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 4, unique = true, updatable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 1, updatable = false)
    private ProgramType type;

    @Column(name = "base_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "adjustment_factor", nullable = false, precision = 6, scale = 4)
    private BigDecimal adjustmentFactor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ProgramStatus status = ProgramStatus.Active;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    @CreatedBy   @Column(name = "created_by", nullable = false, updatable = false, length = 64) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)              private OffsetDateTime createdAt;
    @LastModifiedBy   @Column(name = "updated_by", nullable = false, length = 64)               private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false)                            private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RegionalFactor> regionalFactors = new ArrayList<>();

    protected SocialProgram() {}

    public SocialProgram(String code, ProgramType type, BigDecimal baseAmount,
                         BigDecimal adjustmentFactor, LocalDate effectiveFrom) {
        if (code == null || !code.matches("[A-Z0-9]{4}")) {
            throw new IllegalArgumentException("code must be 4 uppercase alphanumerics; got " + code);
        }
        if (baseAmount == null || baseAmount.signum() < 0) {
            throw new IllegalArgumentException("baseAmount must be ≥ 0");
        }
        validateAdjustmentFactor(adjustmentFactor);
        this.code              = code;
        this.type              = type;
        this.baseAmount        = baseAmount;
        this.adjustmentFactor  = adjustmentFactor;
        this.effectiveFrom     = Objects.requireNonNull(effectiveFrom);
    }

    public void changeAdjustmentFactor(BigDecimal newFactor) {
        if (status.isTerminal()) throw new IllegalStateException("Cannot update a Retired program");
        validateAdjustmentFactor(newFactor);
        this.adjustmentFactor = newFactor;
    }

    public void changeBaseAmount(BigDecimal newBase) {
        if (status.isTerminal()) throw new IllegalStateException("Cannot update a Retired program");
        if (newBase == null || newBase.signum() < 0) throw new IllegalArgumentException("baseAmount must be ≥ 0");
        this.baseAmount = newBase;
    }

    /** Atomically replace the 27-UF regional factor table (FR-008). */
    public void replaceRegionalFactors(Map<String, BigDecimal> ufToFactor) {
        if (status.isTerminal()) throw new IllegalStateException("Cannot update a Retired program");
        if (ufToFactor.size() != BRAZILIAN_UFS.size()
            || !ufToFactor.keySet().containsAll(BRAZILIAN_UFS)) {
            Set<String> missing = new HashSet<>(BRAZILIAN_UFS);
            missing.removeAll(ufToFactor.keySet());
            throw new IllegalArgumentException(
                "Regional factor table must contain exactly the 27 Brazilian UFs. Missing: " + missing);
        }
        this.regionalFactors.clear();
        ufToFactor.forEach((uf, f) -> {
            RegionalFactor rf = new RegionalFactor(uf, f);
            rf.attachTo(this);
            this.regionalFactors.add(rf);
        });
    }

    public void changeStatus(ProgramStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid status transition: %s → %s".formatted(this.status, target));
        }
        this.status = target;
        if (target == ProgramStatus.Retired) this.effectiveUntil = LocalDate.now();
    }

    public Optional<BigDecimal> regionalFactor(String ufCode) {
        return regionalFactors.stream()
            .filter(rf -> rf.getUfCode().equals(ufCode))
            .map(RegionalFactor::getFactor)
            .findFirst();
    }

    private static void validateAdjustmentFactor(BigDecimal v) {
        if (v == null
            || v.compareTo(new BigDecimal("-1.0")) < 0
            || v.compareTo(new BigDecimal("1.0")) > 0) {
            throw new IllegalArgumentException("adjustmentFactor must be in [-1.0, 1.0]");
        }
    }

    public Long getId()                          { return id; }
    public String getCode()                      { return code; }
    public ProgramType getType()                 { return type; }
    public BigDecimal getBaseAmount()            { return baseAmount; }
    public BigDecimal getAdjustmentFactor()      { return adjustmentFactor; }
    public ProgramStatus getStatus()             { return status; }
    public LocalDate getEffectiveFrom()          { return effectiveFrom; }
    public LocalDate getEffectiveUntil()         { return effectiveUntil; }
    public List<RegionalFactor> getRegionalFactors() { return List.copyOf(regionalFactors); }
    public Long getVersion()                     { return version; }
}
