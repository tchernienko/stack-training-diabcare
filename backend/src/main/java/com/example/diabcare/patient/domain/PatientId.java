package com.example.diabcare.patient.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PatientId {

    public static PatientId newOne() {
        return new PatientId();
    }

    public static PatientId of(UUID id) {
        return new PatientId(id);
    }

    private final UUID value;

    private PatientId() {
        this.value = UUID.randomUUID();
    }
}
