package com.example.diabcare.rdv.infrastructure.primary;

import com.example.diabcare.rdv.application.RdvPatientUnknownException;
import com.example.diabcare.rdv.domain.InvalidRdvParametersException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RdvExceptionHandler {

    //cas pour patient inconnu
    @ExceptionHandler
    public ProblemDetail handleRdvPatientUnknownException(RdvPatientUnknownException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "No patient with that id exists: %s".formatted(e.getPatientRef().value().toString())
        );
        detail.setTitle("Unknown patient");
        detail.setProperty("patientRef", e.getPatientRef().value().toString());
        return detail;
    }

    //TODO cas pour une date non strictement futur
    @ExceptionHandler
    public ProblemDetail handleRdvDateNotFuture(InvalidRdvParametersException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            e.getMessage()
        );
        detail.setTitle("Bad Rdv parameters");
        return detail;
    }

}
