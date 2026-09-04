package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.TypePompe;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record RestRequestPatient(
    String nom,
    LocalDate dateNaissance,
    TypePompe typePompe
) {

    public static RestRequestPatient of(Patient patient) {
        return RestRequestPatient.builder()
                .nom(patient.getNom())
                .dateNaissance(patient.getDateNaissance())
                .typePompe(patient.getTypePompe())
                .build();
    }

    public static Patient toDomain(RestRequestPatient restRequestPatient) {
        return Patient.builder()
                .nom(restRequestPatient.nom)
                .dateNaissance(restRequestPatient.dateNaissance)
                .typePompe(restRequestPatient.typePompe)
                .build();
    }


}
