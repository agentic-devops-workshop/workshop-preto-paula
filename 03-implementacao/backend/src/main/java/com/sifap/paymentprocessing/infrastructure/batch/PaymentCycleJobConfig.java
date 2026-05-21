package com.sifap.paymentprocessing.infrastructure.batch;

import com.sifap.paymentprocessing.application.ports.BeneficiarySnapshotPort;
import com.sifap.paymentprocessing.application.ports.SocialProgramQueryPort;
import com.sifap.paymentprocessing.application.ports.AuditEventPublisher;
import com.sifap.paymentprocessing.domain.*;
import com.sifap.paymentprocessing.domain.calculation.PaymentFormula;
import com.sifap.paymentprocessing.domain.events.RegionBypassUsed;
import com.sifap.paymentprocessing.infrastructure.persistence.PaymentCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Spring Batch wiring for the Payment Cycle generation job (ADR-005). */
@Configuration
public class PaymentCycleJobConfig {

    private static final Logger log = LoggerFactory.getLogger(PaymentCycleJobConfig.class);

    @Bean
    Job paymentCycleJob(JobRepository jobRepository, Step generatePaymentsStep) {
        return new JobBuilder("paymentCycleJob", jobRepository)
            .start(generatePaymentsStep)
            .build();
    }

    @Bean
    Step generatePaymentsStep(JobRepository jobRepository,
                              PlatformTransactionManager tx,
                              CyclePaymentReader reader,
                              CyclePaymentProcessor processor,
                              CyclePaymentWriter writer,
                              @Value("${sifap.cycle.chunkSize:1000}") int chunkSize) {
        return new StepBuilder("generatePaymentsStep", jobRepository)
            .<BeneficiarySnapshotPort.Snapshot, Payment>chunk(chunkSize, tx)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skipLimit(Integer.MAX_VALUE)
            .skip(RuntimeException.class)
            .build();
    }

    /** Reads all eligible beneficiaries sorted by CPF (BR-033). */
    @Configuration
    static class CyclePaymentReader implements ItemReader<BeneficiarySnapshotPort.Snapshot> {
        private final BeneficiarySnapshotPort port;
        private java.util.Iterator<BeneficiarySnapshotPort.Snapshot> it;
        @Value("#{jobParameters['programCode']}") private String programCode;

        CyclePaymentReader(BeneficiarySnapshotPort port) { this.port = port; }

        @Override public BeneficiarySnapshotPort.Snapshot read() {
            if (it == null) it = port.streamActiveByProgramSortedByCpf(programCode).iterator();
            return it.hasNext() ? it.next() : null;
        }
    }

    /** Runs the {@link PaymentFormula} and emits region-99 audit events. */
    @Configuration
    static class CyclePaymentProcessor implements ItemProcessor<BeneficiarySnapshotPort.Snapshot, Payment> {
        private final SocialProgramQueryPort programs;
        private final AuditEventPublisher audit;
        private final PaymentCycleRepository cycleRepo;
        @Value("#{jobParameters['cycleId']}")     private Long cycleId;
        @Value("#{jobParameters['competence']}")  private String competenceText;
        @Value("#{jobParameters['programCode']}") private String programCode;

        CyclePaymentProcessor(SocialProgramQueryPort programs, AuditEventPublisher audit,
                              PaymentCycleRepository cycleRepo) {
            this.programs   = programs;
            this.audit      = audit;
            this.cycleRepo  = cycleRepo;
        }

        @Override
        public Payment process(BeneficiarySnapshotPort.Snapshot s) {
            if (!"Active".equals(s.lifecycleStatus())) return null;   // FR-005 skip

            SocialProgramQueryPort.ProgramView program = programs.findActiveByCode(programCode)
                .orElseThrow(() -> new IllegalStateException("Program inactive mid-cycle: " + programCode));

            Competence competence = Competence.parse(competenceText);
            BigDecimal regional = programs.regionalFactor(programCode, s.regionCode());

            PaymentFormula.Input in = new PaymentFormula.Input(
                Money.of(program.baseAmount()),
                regional,
                s.dependentCount(),
                s.familyIncome(),
                s.birthDate(),
                program.adjustmentFactor(),
                program.isTypeA(),
                competence,
                LocalDate.now()
            );
            PaymentFormula.Result r = PaymentFormula.compute(in);

            boolean regionBypass = (s.regionCode() == 99);
            if (regionBypass) {
                PaymentCycle cycle = cycleRepo.getReferenceById(cycleId);
                audit.publish(new RegionBypassUsed(
                    cycleId, cycle.getCorrelationId(),
                    "XXX.XXX.XXX-" + s.cpf().substring(9),
                    competenceText, OffsetDateTime.now()));
            }

            return new Payment(
                cycleId, s.id(), s.cpf(), competenceText, programCode,
                competence.isDecember() ? PaymentType.THIRTEENTH : PaymentType.MONTHLY,
                r.grossAmount().asBigDecimal(),
                r.christmasAllowance().asBigDecimal(),
                serialize(r.factors()),
                s.regionCode(),
                regionBypass
            );
        }

        private static Map<String, Object> serialize(PaymentFormula.Factors f) {
            Map<String, Object> m = new HashMap<>();
            m.put("base",     f.base().asBigDecimal());
            m.put("regional", f.regional());
            m.put("family",   f.family());
            m.put("income",   f.income());
            m.put("age",      f.age());
            m.put("kFactor",  f.kFactor());
            return m;
        }
    }

    /** Bulk JPA writer; the unique index (cpf, competence, program_code) provides idempotency. */
    @Configuration
    static class CyclePaymentWriter implements ItemWriter<Payment> {
        private final jakarta.persistence.EntityManager em;
        CyclePaymentWriter(jakarta.persistence.EntityManager em) { this.em = em; }
        @Override public void write(org.springframework.batch.item.Chunk<? extends Payment> chunk) {
            for (Payment p : chunk) em.persist(p);
            em.flush();
            em.clear();
            log.debug("Wrote chunk of {} payments", chunk.size());
        }
    }
}
