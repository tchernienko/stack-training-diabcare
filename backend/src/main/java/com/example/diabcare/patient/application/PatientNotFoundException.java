package com.example.diabcare.patient.application;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PatientNotFoundException extends RuntimeException {

    private final UUID idNotFound;

    public PatientNotFoundException(UUID idNotFound, String message) {
        super(message);
        this.idNotFound = idNotFound;
    }

}
