package com.sifap.paymentprocessing.application;

/**
 * Outbound port used by {@link PaymentCycleService} to start and stop
 * a cycle execution. Implemented by Spring Batch glue under
 * {@code infrastructure.batch}.
 */
public interface CycleJobLauncher {
    void launch(long cycleId);
    void stop(long cycleId);
}
