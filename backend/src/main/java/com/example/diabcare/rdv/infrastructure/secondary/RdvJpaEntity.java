package com.example.diabcare.rdv.infrastructure.secondary;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvId;
import com.example.diabcare.rdv.domain.RdvStatut;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rdv")
public class RdvJpaEntity {

    public static RdvJpaEntity of(Rdv rdv) {
        return new RdvJpaEntity(rdv.id().value(), rdv.patientRef().value(), rdv.dateHeure(), rdv.statut());
    }

    private UUID id;
    private UUID patientRef;
    private LocalDateTime dateHeure;
    private RdvStatut rdvStatut;

    public RdvJpaEntity() {
    }

    public RdvJpaEntity(UUID id, UUID patientRef, LocalDateTime dateHeure, RdvStatut rdvStatut) {
        this.id = id;
        this.patientRef = patientRef;
        this.dateHeure = dateHeure;
        this.rdvStatut = rdvStatut;
    }

    @Id
    @Column(name = "id", length = 36)
    public UUID getId() {
        return id;
    }

    public RdvJpaEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    @Column(name = "patient_ref", length = 36)
    public UUID getPatientRef() {
        return patientRef;
    }

    public RdvJpaEntity setPatientRef(UUID patientRef) {
        this.patientRef = patientRef;
        return this;
    }

    @Column(name = "date_heure")
    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public RdvJpaEntity setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
        return this;
    }

    @Column(name = "statut")
    @Enumerated(value = EnumType.STRING)
    public RdvStatut getRdvStatut() {
        return rdvStatut;
    }

    public RdvJpaEntity setRdvStatut(RdvStatut rdvStatut) {
        this.rdvStatut = rdvStatut;
        return this;
    }

    public Rdv toDomain() {
        return new Rdv(
            RdvId.of(this.id), PatientRef.of(this.patientRef), this.dateHeure, this.rdvStatut
        );
    }

}
