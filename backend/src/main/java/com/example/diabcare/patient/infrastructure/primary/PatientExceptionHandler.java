package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.application.PatientNotFoundException;
import com.example.diabcare.patient.domain.InvalidPatientParametersException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PatientExceptionHandler {

    //validation metier (constructeur de Patient du domaine): nom manquant, dateNaissance aujourd'hui ou future, typePompe absent
    @ExceptionHandler(InvalidPatientParametersException.class)
    public ProblemDetail onInvalidPatient(InvalidPatientParametersException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        detail.setTitle("Paramètres patient invalides");
        detail.setProperty("field", e.getField());
        return detail;
    }

    //cas pour le type de pompe inconnu ou json invalide
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadableBody(HttpMessageNotReadableException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corps de requête mal formé ou invalide");
        detail.setTitle("Requête illisible/invalide");
        return detail;
    }

    //cas pour le findById -> not found
    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail onPatientNotFound(PatientNotFoundException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        detail.setTitle("Patient inexistant");
        detail.setDetail("Pas de patient avec cet id %s".formatted(e.getIdNotFound()));
        detail.setProperty("idNotFound", e.getIdNotFound());
        return detail;
    }

}
