package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.TypePompe;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RestResponsePatient {

    private RestPatientId id;
    private String nom;
    private LocalDate dateNaissance;
    private TypePompe typePompe;

    public static RestResponsePatient of(Patient patient) {
        return RestResponsePatient.builder()
                .id(RestPatientId.of(patient.getId().getValue()))
                .nom(patient.getNom())
                .dateNaissance(patient.getDateNaissance())
                .typePompe(patient.getTypePompe())
                .build();
    }

}
