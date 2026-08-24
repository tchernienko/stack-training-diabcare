package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.TypePompe;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
public class RestRequestPatient {

    private String nom;
    private LocalDate dateNaissance;
    private TypePompe typePompe;

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
