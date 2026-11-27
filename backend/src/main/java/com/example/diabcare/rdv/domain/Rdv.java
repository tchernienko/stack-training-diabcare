package com.example.diabcare.rdv.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record Rdv(RdvId id, PatientRef patientRef, LocalDateTime dateHeure, RdvStatut statut) {

    public static Rdv planifie(PatientRef patientRef, LocalDateTime dateHeure) {
        Rdv rdv = new Rdv(RdvId.newOne(), patientRef, dateHeure, RdvStatut.PLANIFIE);
        checkRdvStatusVsDateHeure(rdv.statut, rdv.dateHeure);
        return rdv;
    }

    //garder le param statut mm si toujours la mm valeur, par crainte de l'oublier
    static void checkRdvStatusVsDateHeure(RdvStatut statut, LocalDateTime dateHeure) {
        if (statut == RdvStatut.PLANIFIE && dateHeure.isBefore(LocalDateTime.now())) {
            throw new InvalidRdvParametersException("Rdv status is planifie and dateHeure is in the past. A planifie Rdv cannot be in the past.");
        }
    }

    public Rdv(RdvId id, PatientRef patientRef, LocalDateTime dateHeure, RdvStatut statut) {
        this.id = Objects.requireNonNull(id, "id must not be null");

        if (patientRef == null) {
            throw new InvalidRdvParametersException("Patient ref must not be null");
        }
        this.patientRef = patientRef;

        if (dateHeure == null) {
            throw new InvalidRdvParametersException("dateHeure must not be null");
        }
        this.dateHeure = dateHeure;

        if (statut == null) {
            throw new NullPointerException("RdvStatut must not be null");
        }
        if (statut == RdvStatut.HONORE && dateHeure.isAfter(LocalDateTime.now())) {
            throw new InvalidRdvParametersException("Rdv status is honore and dateHeure is in the future. A honore Rdv cannot be in the future.");
        }
        //ne pas tester le statut annulé dans le futur, car un rdv futur peut être annulé
        this.statut = statut;

    }

    public static class Builder {
        private RdvId id;
        private PatientRef patientRef;
        private LocalDateTime dateHeure;
        private RdvStatut statut;

        public Rdv build() {
            return new Rdv(this.id, this.patientRef, this.dateHeure, this.statut);
        }

        public Builder id(RdvId id) {
            this.id = id;
            return this;
        }

        public Builder patientRef(PatientRef patientRef) {
            this.patientRef = patientRef;
            return this;
        }

        public Builder dateHeure(LocalDateTime dateHeure) {
            this.dateHeure = dateHeure;
            return this;
        }

        public Builder statut(RdvStatut statut) {
            this.statut = statut;
            return this;
        }
    }

}
