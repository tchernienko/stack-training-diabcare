package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.PatientRef;

public class RdvPatientUnknownException extends RuntimeException {

    private final PatientRef patientRef;

    public RdvPatientUnknownException(PatientRef patientRef, String message) {
        super(message);
        this.patientRef = patientRef;
    }

    public PatientRef getPatientRef() {
        return patientRef;
    }
}
