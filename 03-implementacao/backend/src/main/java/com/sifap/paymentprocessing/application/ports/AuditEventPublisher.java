package com.sifap.paymentprocessing.application.ports;

/** Outbound port for audit events consumed by ReportingAndAudit. */
public interface AuditEventPublisher {
    void publish(Object event);
}
