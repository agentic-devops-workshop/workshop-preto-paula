package com.sifap.socialprogram.interfaces;

import com.sifap.socialprogram.application.SocialProgramService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(basePackages = "com.sifap.socialprogram")
public class SocialProgramExceptionHandler {

    @ExceptionHandler(SocialProgramService.ProgramNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(SocialProgramService.ProgramNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "PROGRAM_NOT_FOUND",
                       "https://sifap.gov.br/errors/program-not-found", ex.getMessage());
    }

    @ExceptionHandler(SocialProgramService.ProgramAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleConflict(SocialProgramService.ProgramAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "PROGRAM_ALREADY_EXISTS",
                       "https://sifap.gov.br/errors/program-already-exists", ex.getMessage());
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
