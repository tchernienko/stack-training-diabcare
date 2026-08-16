package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.TypePompe;
import lombok.Builder;
import org.apache.commons.lang3.NotImplementedException;

import java.time.LocalDate;

@Builder
public class RestPatient {

    private RestPatientId patientId;
    private String nom;
    private LocalDate dateNaissance;
    private TypePompe typePompe;

    public static RestPatient of(Patient patient) {
        return RestPatient.builder()
                .patientId(RestPatientId.of(patient.getId().getValeur()))
                .nom(patient.getNom())
                .dateNaissance(patient.getDateNaissance())
                .typePompe(patient.getTypePompe())
                .build();
    }

    //TODO voir pour implémenter ça si besoin
    public static Patient toDomain(RestPatient restPatient) {
        throw new NotImplementedException();
    }

}
