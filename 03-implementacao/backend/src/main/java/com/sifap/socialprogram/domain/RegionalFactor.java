package com.sifap.socialprogram.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "regional_factor",
       uniqueConstraints = @UniqueConstraint(name = "uq_program_uf", columnNames = {"program_id","uf_code"}))
public class RegionalFactor {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private SocialProgram program;

    @Column(name = "uf_code", nullable = false, length = 2)
    private String ufCode;

    @Column(name = "factor", nullable = false, precision = 5, scale = 3)
    private BigDecimal factor;

    protected RegionalFactor() {}

    public RegionalFactor(String ufCode, BigDecimal factor) {
        if (ufCode == null || !ufCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("ufCode must be 2 uppercase letters; got " + ufCode);
        }
        if (factor == null || factor.compareTo(new BigDecimal("0.5")) < 0 || factor.compareTo(new BigDecimal("2.0")) > 0) {
            throw new IllegalArgumentException("factor must be in [0.5, 2.0]");
        }
        this.ufCode = ufCode;
        this.factor = factor;
    }

    void attachTo(SocialProgram p) { this.program = Objects.requireNonNull(p); }

    public String getUfCode()    { return ufCode; }
    public BigDecimal getFactor() { return factor; }
}
