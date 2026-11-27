package com.example.diabcare.rdv.infrastructure.primary;

import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvStatut;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.LocalDateTime;
import java.util.UUID;


public record RestResponseRdv(
    UUID id, RestPatientRef patientRef, LocalDateTime dateHeure, RdvStatut statut) {

    @JsonCreator
    public static RestResponseRdv of(Rdv rdv) {
        return new RestResponseRdv(rdv.id().value(), RestPatientRef.of(rdv.patientRef().value()), rdv.dateHeure(), rdv.statut());
    }

}
