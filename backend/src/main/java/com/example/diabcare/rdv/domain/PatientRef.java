package com.example.diabcare.rdv.domain;

import java.util.UUID;

/**
 * record used to link the rdv domain to the patient domain without depending on it.
 * Other domains can create PatientRef from PatientId using application layer classes.
 * @param value
 */
public record PatientRef(UUID value) {

    public static PatientRef of(UUID value) {
        return new PatientRef(value);
    }

}
