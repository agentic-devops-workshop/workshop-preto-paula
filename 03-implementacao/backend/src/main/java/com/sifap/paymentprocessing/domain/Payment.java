package com.sifap.paymentprocessing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

/** A single payment row produced by a cycle. */
@Entity
@Table(name = "payment")
@IdClass(Payment.PaymentId.class)
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Id
    @Column(name = "competence", nullable = false, length = 7, insertable = false, updatable = false)
    private String competence;

    @Column(name = "cycle_id", nullable = false)        private Long cycleId;
    @Column(name = "beneficiary_id", nullable = false)  private Long beneficiaryId;
    @Column(name = "cpf", nullable = false, length = 11) private String cpf;
    @Column(name = "program_code", nullable = false, length = 4) private String programCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 10)
    private PaymentType paymentType;

    @Column(name = "gross_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "christmas_allowance", nullable = false, precision = 15, scale = 2)
    private BigDecimal christmasAllowance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status = PaymentStatus.Pending;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "factors", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> factors;

    @Column(name = "region_code", nullable = false)        private Short regionCode;
    @Column(name = "region_bypass", nullable = false)      private boolean regionBypass;
    @Column(name = "generated_at", nullable = false)       private OffsetDateTime generatedAt = OffsetDateTime.now();
    @Column(name = "voided_at")                            private OffsetDateTime voidedAt;

    protected Payment() {}

    public Payment(Long cycleId, Long beneficiaryId, String cpf, String competence, String programCode,
                   PaymentType type, BigDecimal grossAmount, BigDecimal christmasAllowance,
                   Map<String, Object> factors, Short regionCode, boolean regionBypass) {
        this.cycleId            = cycleId;
        this.beneficiaryId      = beneficiaryId;
        this.cpf                = cpf;
        this.competence         = competence;
        this.programCode        = programCode;
        this.paymentType        = type;
        this.grossAmount        = grossAmount;
        this.christmasAllowance = christmasAllowance == null ? BigDecimal.ZERO : christmasAllowance;
        this.factors            = factors;
        this.regionCode         = regionCode;
        this.regionBypass       = regionBypass;
    }

    public void voidPayment() {
        this.status = PaymentStatus.Voided;
        this.voidedAt = OffsetDateTime.now();
    }

    public Long getId()                          { return id; }
    public Long getCycleId()                     { return cycleId; }
    public Long getBeneficiaryId()               { return beneficiaryId; }
    public String getCpf()                       { return cpf; }
    public String getCompetence()                { return competence; }
    public String getProgramCode()               { return programCode; }
    public PaymentType getPaymentType()          { return paymentType; }
    public BigDecimal getGrossAmount()           { return grossAmount; }
    public BigDecimal getChristmasAllowance()    { return christmasAllowance; }
    public PaymentStatus getStatus()             { return status; }
    public Map<String, Object> getFactors()      { return factors; }
    public Short getRegionCode()                 { return regionCode; }
    public boolean isRegionBypass()              { return regionBypass; }

    /** Composite ID matches the partition key (id, competence). */
    public static class PaymentId implements java.io.Serializable {
        private Long id;
        private String competence;
        public PaymentId() {}
        public PaymentId(Long id, String competence) { this.id = id; this.competence = competence; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof PaymentId p)) return false;
            return java.util.Objects.equals(id, p.id) && java.util.Objects.equals(competence, p.competence);
        }
        @Override public int hashCode() { return java.util.Objects.hash(id, competence); }
    }
}
