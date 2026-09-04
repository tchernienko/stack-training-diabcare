package com.example.diabcare.patient.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
public class Patient {

    private final PatientId id;
    private final String nom;
    private final LocalDate dateNaissance;
    private final TypePompe typePompe;

    /**
     * Returns a valid Patient with given fields. All fields are required except id.
     * @param id
     * @param nom
     * @param dateNaissance
     * @param typePompe
     * @throws InvalidPatientParametersException if nom is blank or null, dateNaissance null or today or in the future, typePompe is null.
     */
    @Builder
    public Patient(PatientId id, String nom, LocalDate dateNaissance, TypePompe typePompe) {

        if (id == null) {
            this.id = PatientId.newOne();
        } else {
            this.id = id;
        }

        if (StringUtils.isBlank(nom)) {
            throw new InvalidPatientParametersException("nom", "Valeur obligatoire.");
        }
        this.nom = nom;

        if (dateNaissance == null) {
            throw new InvalidPatientParametersException("dateNaissance", "Valeur obligatoire.");
        }
        if (dateNaissance.isAfter(LocalDate.now()) || dateNaissance.isEqual(LocalDate.now())) {
            throw new InvalidPatientParametersException("dateNaissance", "non strictement dans le passé.");
        }
        this.dateNaissance = dateNaissance;

        if (typePompe == null) {
            throw new InvalidPatientParametersException("typePompe", "Le type de pompe n'est pas valide, valeur non vide ou valeur connue (PATCH, TUBULAIRE) attendue.");
        }
        this.typePompe = typePompe;
    }
}
