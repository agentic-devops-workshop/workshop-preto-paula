package com.sifap.paymentprocessing.interfaces;

import com.sifap.paymentprocessing.application.PaymentCycleService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(basePackages = "com.sifap.paymentprocessing")
public class PaymentCycleExceptionHandler {

    @ExceptionHandler(PaymentCycleService.CycleAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflict(PaymentCycleService.CycleAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "CYCLE_ALREADY_GENERATED",
                       "https://sifap.gov.br/errors/cycle-already-generated", ex.getMessage());
    }

    @ExceptionHandler(PaymentCycleService.CycleNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(PaymentCycleService.CycleNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "CYCLE_NOT_FOUND",
                       "https://sifap.gov.br/errors/cycle-not-found", ex.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String typeUri, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(typeUri));
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("code", code);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) pd.setProperty("correlationId", correlationId);
        return ResponseEntity.status(status).body(pd);
    }
}
