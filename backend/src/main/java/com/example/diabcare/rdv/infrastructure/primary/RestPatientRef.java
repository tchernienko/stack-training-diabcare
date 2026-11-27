package com.example.diabcare.rdv.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

public record RestPatientRef(UUID value) {

    @JsonCreator
    public static RestPatientRef of(UUID value) {
        return new RestPatientRef(value);
    }

    @JsonValue
    public UUID value() {
        return value;
    }

}
