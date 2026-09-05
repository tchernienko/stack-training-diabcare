package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.TypePompe;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RestResponsePatient(
    RestPatientId id,
    String nom,
    LocalDate dateNaissance,
    TypePompe typePompe
) {

    public static RestResponsePatient of(Patient patient) {
        return RestResponsePatient.builder()
                .id(RestPatientId.of(patient.getId().getValue()))
                .nom(patient.getNom())
                .dateNaissance(patient.getDateNaissance())
                .typePompe(patient.getTypePompe())
                .build();
    }

}
