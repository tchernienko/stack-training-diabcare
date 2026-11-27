package com.example.diabcare.rdv.infrastructure.primary;

import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvStatut;

import java.time.LocalDateTime;

public record RestRdvRequest(RestRdvId id, RestPatientRef patientRef, LocalDateTime dateHeure, RdvStatut statut) {
    public static RestRdvRequest of(Rdv rdv) {
        return new RestRdvRequest(
            RestRdvId.of(rdv.id().value()),
            RestPatientRef.of(rdv.patientRef().value()),
            rdv.dateHeure(),
            rdv.statut());
    }
}
