package com.sifap.beneficiary.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dependent")
public class Dependent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Column(name = "cpf", length = 11)
    private String cpf;                         // nullable (BR-009 for children)

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "parentage", nullable = false, length = 10)
    private Parentage parentage;

    @Column(name = "document", length = 20)
    private String document;

    @Column(name = "sex", length = 1)
    private String sex;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpf_validation_status", nullable = false, length = 20)
    private CpfValidationStatus cpfValidationStatus = CpfValidationStatus.VALID;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "source_legacy_isn", updatable = false)
    private Long sourceLegacyIsn;

    protected Dependent() {}

    public Dependent(String name, LocalDate birthDate, Parentage parentage) {
        this.name      = name;
        this.birthDate = birthDate;
        this.parentage = parentage;
    }

    void attachTo(Beneficiary beneficiary) { this.beneficiary = beneficiary; }

    public Long getId()                                  { return id; }
    public String getCpf()                               { return cpf; }
    public String getName()                              { return name; }
    public LocalDate getBirthDate()                      { return birthDate; }
    public Parentage getParentage()                      { return parentage; }
    public CpfValidationStatus getCpfValidationStatus()  { return cpfValidationStatus; }
}
