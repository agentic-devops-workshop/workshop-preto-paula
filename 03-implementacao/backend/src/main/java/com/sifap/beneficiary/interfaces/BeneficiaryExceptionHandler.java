package com.sifap.beneficiary.interfaces;

import com.sifap.beneficiary.application.BeneficiaryService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(basePackages = "com.sifap.beneficiary")
public class BeneficiaryExceptionHandler {

    @ExceptionHandler(BeneficiaryService.BeneficiaryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(BeneficiaryService.BeneficiaryNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND",
                       "https://sifap.gov.br/errors/beneficiary-not-found", ex.getMessage());
    }

    @ExceptionHandler(BeneficiaryService.BeneficiaryAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflict(BeneficiaryService.BeneficiaryAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "BENEFICIARY_ALREADY_EXISTS",
                       "https://sifap.gov.br/errors/beneficiary-already-exists", ex.getMessage());
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
