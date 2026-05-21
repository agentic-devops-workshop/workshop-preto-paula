package com.sifap.beneficiary.application;

import com.sifap.beneficiary.domain.*;
import com.sifap.beneficiary.infrastructure.persistence.BeneficiaryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository repository;
    private final BrazilianCpfValidator cpfValidator;

    public BeneficiaryService(BeneficiaryRepository repository,
                              @Value("${sifap.cpf.allowTestCpfs:false}") boolean allowTestCpfs) {
        this.repository   = repository;
        this.cpfValidator = new BrazilianCpfValidator(allowTestCpfs);
    }

    public Beneficiary register(String rawCpf, String name, LocalDate birthDate,
                                String sex, String programCode) {
        Cpf cpf = Cpf.parse(rawCpf);
        CpfValidationStatus cpfStatus = cpfValidator.validate(cpf);

        if (repository.existsByCpf(cpf.digits())) {
            throw new BeneficiaryAlreadyExistsException(cpf.digits());
        }

        AgeCategory ageCat = AgeCategory.of(birthDate, LocalDate.now());
        Beneficiary b = new Beneficiary(cpf.digits(), name, birthDate, sex, programCode, ageCat, cpfStatus);
        return repository.save(b);
    }

    @Transactional(readOnly = true)
    public Beneficiary findByCpf(String rawCpf) {
        Cpf cpf = Cpf.parse(rawCpf);
        return repository.findByCpf(cpf.digits())
            .orElseThrow(() -> new BeneficiaryNotFoundException(cpf.digits()));
    }

    public void changeLifecycleStatus(String rawCpf, LifecycleStatus target, String reason) {
        Beneficiary b = findByCpf(rawCpf);
        b.changeLifecycleStatus(target, reason);
    }

    /** Soft delete (CL-004). */
    public void cancel(String rawCpf, String reason) {
        Beneficiary b = findByCpf(rawCpf);
        b.softDelete(reason);
    }

    public Dependent addDependent(String beneficiaryCpf, String name, LocalDate birthDate,
                                  Parentage parentage) {
        Beneficiary b = findByCpf(beneficiaryCpf);
        Dependent d = new Dependent(name, birthDate, parentage);
        b.addDependent(d);
        return d;
    }

    public static class BeneficiaryAlreadyExistsException extends RuntimeException {
        public BeneficiaryAlreadyExistsException(String cpf) {
            super("Beneficiary with CPF " + BrazilianCpfMasker.mask(cpf) + " already exists");
        }
    }

    public static class BeneficiaryNotFoundException extends RuntimeException {
        public BeneficiaryNotFoundException(String cpf) {
            super("Beneficiary with CPF " + BrazilianCpfMasker.mask(cpf) + " not found");
        }
    }
}
