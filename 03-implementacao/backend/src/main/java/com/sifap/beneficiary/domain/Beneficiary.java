package com.sifap.beneficiary.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Aggregate root of the Beneficiary bounded context. */
@Entity
@Table(name = "beneficiary")
@EntityListeners(AuditingEntityListener.class)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf", nullable = false, length = 11, unique = true, updatable = false)
    private String cpf;

    @Column(name = "nis", length = 11, unique = true)
    private String nis;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "sex", nullable = false, length = 1)
    private String sex;

    @Column(name = "rg", length = 20)
    private String rg;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifecycle_status", nullable = false, length = 15)
    private LifecycleStatus lifecycleStatus = LifecycleStatus.Active;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category", nullable = false, length = 10)
    private AgeCategory ageCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "cpf_validation_status", nullable = false, length = 20)
    private CpfValidationStatus cpfValidationStatus = CpfValidationStatus.VALID;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private OffsetDateTime registeredAt = OffsetDateTime.now();

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @CreatedBy   @Column(name = "created_by", nullable = false, updatable = false, length = 64) private String createdBy;
    @CreatedDate @Column(name = "created_at", nullable = false, updatable = false)              private OffsetDateTime createdAt;
    @LastModifiedBy   @Column(name = "updated_by", nullable = false, length = 64)               private String updatedBy;
    @LastModifiedDate @Column(name = "updated_at", nullable = false)                            private OffsetDateTime updatedAt;

    @Version @Column(name = "version", nullable = false) private Long version;

    @Column(name = "source_legacy_isn", unique = true, updatable = false)
    private Long sourceLegacyIsn;

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dependent> dependents = new ArrayList<>();

    protected Beneficiary() {}

    public Beneficiary(String cpf, String name, LocalDate birthDate, String sex,
                       String programCode, AgeCategory ageCategory,
                       CpfValidationStatus cpfValidationStatus) {
        validateName(name);
        this.cpf = Objects.requireNonNull(cpf);
        this.name = name;
        this.birthDate = Objects.requireNonNull(birthDate);
        this.sex = Objects.requireNonNull(sex);
        this.programCode = Objects.requireNonNull(programCode);
        this.ageCategory = Objects.requireNonNull(ageCategory);
        this.cpfValidationStatus = Objects.requireNonNull(cpfValidationStatus);
    }

    /** Compose name has ≥ 2 whitespace-separated tokens (BR-030, FR-006). */
    private static void validateName(String name) {
        if (name == null || name.trim().split("\\s+").length < 2) {
            throw new IllegalArgumentException("Name must include first name and surname");
        }
    }

    public void changeLifecycleStatus(LifecycleStatus target, String reason) {
        if (!this.lifecycleStatus.canTransitionTo(target)) {
            throw new IllegalStateException(
                "Invalid lifecycle transition: %s → %s".formatted(this.lifecycleStatus, target));
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        this.lifecycleStatus = target;
        if (target == LifecycleStatus.Cancelled) {
            this.cancelledAt = OffsetDateTime.now();
        }
    }

    /** Soft delete per CL-004. */
    public void softDelete(String reason) { changeLifecycleStatus(LifecycleStatus.Cancelled, reason); }

    public void addDependent(Dependent dep) {
        if (this.lifecycleStatus != LifecycleStatus.Active) {
            throw new IllegalStateException("Cannot add dependents to a non-Active beneficiary");
        }
        if (this.dependents.size() >= 5) {   // CL-001 default; final value parameterized per program
            throw new IllegalStateException("Maximum of 5 dependents reached");
        }
        dep.attachTo(this);
        this.dependents.add(dep);
    }

    public void recomputeAgeCategory(LocalDate today) {
        this.ageCategory = AgeCategory.of(this.birthDate, today);
    }

    // Getters (no setters — mutations go through behavior methods above)
    public Long getId()                                    { return id; }
    public String getCpf()                                 { return cpf; }
    public String getNis()                                 { return nis; }
    public String getName()                                { return name; }
    public LocalDate getBirthDate()                        { return birthDate; }
    public String getSex()                                 { return sex; }
    public String getRg()                                  { return rg; }
    public Address getAddress()                            { return address; }
    public LifecycleStatus getLifecycleStatus()            { return lifecycleStatus; }
    public AgeCategory getAgeCategory()                    { return ageCategory; }
    public CpfValidationStatus getCpfValidationStatus()    { return cpfValidationStatus; }
    public String getProgramCode()                         { return programCode; }
    public OffsetDateTime getRegisteredAt()                { return registeredAt; }
    public OffsetDateTime getCancelledAt()                 { return cancelledAt; }
    public Long getSourceLegacyIsn()                       { return sourceLegacyIsn; }
    public List<Dependent> getDependents()                 { return List.copyOf(dependents); }
    public Long getVersion()                               { return version; }

    public void assignNis(String nis)            { this.nis = nis; }
    public void updateContactInfo(Address addr)  { this.address = addr; }
    public void updateRg(String rg)              { this.rg = rg; }
}
