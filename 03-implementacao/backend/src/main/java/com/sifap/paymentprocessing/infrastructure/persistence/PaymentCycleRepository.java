package com.sifap.paymentprocessing.infrastructure.persistence;

import com.sifap.paymentprocessing.domain.PaymentCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentCycleRepository extends JpaRepository<PaymentCycle, Long> {
    Optional<PaymentCycle> findByCompetenceAndProgramCode(String competence, String programCode);
    boolean existsByCompetenceAndProgramCode(String competence, String programCode);
}
