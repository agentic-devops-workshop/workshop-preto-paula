package com.sifap.eligibility.interfaces;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(assignableTypes = EligibilityController.class)
public class EligibilityExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex) {
        return problem(HttpStatus.BAD_REQUEST, "ELIGIBILITY_VALIDATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(EligibilityController.RateLimitExceededException.class)
    public ResponseEntity<ProblemDetail> handleRateLimit(EligibilityController.RateLimitExceededException ex) {
        return problem(HttpStatus.TOO_MANY_REQUESTS, "ELIGIBILITY_RATE_LIMITED", ex.getMessage());
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setType(URI.create("https://sifap.gov.br/errors/" + code.toLowerCase().replace('_', '-')));
        body.setTitle(status.getReasonPhrase());
        body.setProperty("code", code);
        return ResponseEntity.status(status).body(body);
    }
}