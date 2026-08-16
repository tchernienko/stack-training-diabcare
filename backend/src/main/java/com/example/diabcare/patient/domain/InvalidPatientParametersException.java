package com.example.diabcare.patient.domain;

public class InvalidPatientParametersException extends RuntimeException {
    public InvalidPatientParametersException(String message) {
        super(message);
    }
}
