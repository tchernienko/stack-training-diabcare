package com.example.diabcare.patient.infrastructure.primary;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class RestPatientId {

    private UUID value;

    public static RestPatientId of(UUID patientId) {
        return new RestPatientId(patientId);
    }

}
