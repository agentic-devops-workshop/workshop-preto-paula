package com.sifap.beneficiary.infrastructure.persistence;

import com.sifap.beneficiary.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    Optional<Beneficiary> findByCpf(String cpf);
    Optional<Beneficiary> findByNis(String nis);
    boolean existsByCpf(String cpf);
}
