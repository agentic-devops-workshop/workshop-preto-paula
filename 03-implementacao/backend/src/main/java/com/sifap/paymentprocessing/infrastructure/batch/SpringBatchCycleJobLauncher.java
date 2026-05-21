package com.sifap.paymentprocessing.infrastructure.batch;

import com.sifap.paymentprocessing.application.CycleJobLauncher;
import com.sifap.paymentprocessing.domain.PaymentCycle;
import com.sifap.paymentprocessing.infrastructure.persistence.PaymentCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

/** Spring Batch implementation of {@link CycleJobLauncher}. */
@Component
public class SpringBatchCycleJobLauncher implements CycleJobLauncher {

    private static final Logger log = LoggerFactory.getLogger(SpringBatchCycleJobLauncher.class);

    private final JobLauncher launcher;
    private final JobOperator operator;
    private final Job paymentCycleJob;
    private final PaymentCycleRepository repository;

    public SpringBatchCycleJobLauncher(JobLauncher launcher, JobOperator operator,
                                       Job paymentCycleJob, PaymentCycleRepository repository) {
        this.launcher        = launcher;
        this.operator        = operator;
        this.paymentCycleJob = paymentCycleJob;
        this.repository      = repository;
    }

    @Override
    public void launch(long cycleId) {
        PaymentCycle cycle = repository.findById(cycleId).orElseThrow();
        cycle.start();
        JobParameters params = new JobParametersBuilder()
            .addLong("cycleId", cycleId)
            .addString("competence", cycle.getCompetence())
            .addString("programCode", cycle.getProgramCode())
            .addString("correlationId", cycle.getCorrelationId().toString())
            .toJobParameters();
        try {
            launcher.run(paymentCycleJob, params);
        } catch (Exception ex) {
            log.error("Failed to launch payment cycle job for id={}", cycleId, ex);
            throw new IllegalStateException("Cycle launch failed", ex);
        }
    }

    @Override
    public void stop(long cycleId) {
        try {
            operator.getRunningExecutions("paymentCycleJob").forEach(execId -> {
                try { operator.stop(execId); } catch (Exception ignored) {}
            });
        } catch (Exception ex) {
            log.warn("Failed to stop running cycle id={}", cycleId, ex);
        }
    }
}
