package com.sifap.shared.error;

import com.sifap.beneficiary.application.BeneficiaryService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/** RFC 7807 error responses for all controllers. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BeneficiaryService.BeneficiaryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(BeneficiaryService.BeneficiaryNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "BENEFICIARY_NOT_FOUND", ex.getMessage(),
                       "https://sifap.gov.br/errors/beneficiary-not-found");
    }

    @ExceptionHandler(BeneficiaryService.BeneficiaryAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflict(BeneficiaryService.BeneficiaryAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "BENEFICIARY_ALREADY_EXISTS", ex.getMessage(),
                       "https://sifap.gov.br/errors/beneficiary-already-exists");
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> handleValidation(RuntimeException ex) {
        HttpStatus status = ex instanceof IllegalStateException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return problem(status, "VALIDATION_FAILED", ex.getMessage(),
                       "https://sifap.gov.br/errors/validation");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail, String typeUri) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(typeUri));
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("code", code);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) pd.setProperty("correlationId", correlationId);
        return ResponseEntity.status(status).body(pd);
    }
}
