package com.example.diabcare.patient.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
@Builder
public class Patient {

    private PatientId id;
    private String nom;
    private LocalDate dateNaissance;
    private TypePompe typePompe;

    @Builder
    public Patient(PatientId id, String nom, LocalDate dateNaissance, TypePompe typePompe) {

        if (id == null) {
            this.id = PatientId.newOne();
        } else {
            this.id = id;
        }

        if (StringUtils.isBlank(nom)) {
            throw new InvalidPatientParametersException("Le nom n'est pas valide, valeur non vide attendue.");
        }
        this.nom = nom;

        if (dateNaissance.isAfter(LocalDate.now())) {
            throw new InvalidPatientParametersException("La date de naissance n'est pas valide, valeur dans le futur interdite.");
        }
        this.dateNaissance = dateNaissance;

        if (typePompe == null) {
            throw new InvalidPatientParametersException("Le type de pompe n'est pas valide, valeur non vide attendue.");
        }
        this.typePompe = typePompe;
    }
}
