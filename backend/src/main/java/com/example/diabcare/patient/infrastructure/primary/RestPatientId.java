package com.example.diabcare.patient.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

public record RestPatientId(UUID value) {

    @JsonCreator
    public static RestPatientId of(UUID patientId) {
        return new RestPatientId(patientId);
    }

    @Override
    @JsonValue
    public UUID value() {
        return value;
    }

}
