package com.example.diabcare.rdv.infrastructure.primary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;

public record RestRdvId(UUID value) {

    @JsonCreator
    public static RestRdvId of(UUID value) {
        return new RestRdvId(value);
    }

    @Override
    @JsonValue
    public UUID value() {
        return value;
    }

}
