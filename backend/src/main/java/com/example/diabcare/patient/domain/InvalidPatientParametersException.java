package com.example.diabcare.patient.domain;

import lombok.Getter;

@Getter
public class InvalidPatientParametersException extends RuntimeException {

    private final String field;

    public InvalidPatientParametersException(String field, String message) {
        super("Erreur sur le champ %s, car %s".formatted(field, message));
        this.field = field;
    }

}
