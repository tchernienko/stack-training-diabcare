package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import com.example.diabcare.patient.domain.TypePompe;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "patient")
public class PatientJpaEntity {

    private UUID id;

    private String nom;

    private LocalDate dateNaissance;

    private TypePompe typePompe;

    public static PatientJpaEntity of(Patient patient) {
        return new PatientJpaEntity()
                .setId(patient.getId().getValue())
                .setNom(patient.getNom())
                .setDateNaissance(patient.getDateNaissance())
                .setTypePompe(patient.getTypePompe());
    }

    @Transient
    public Patient toDomain() {
        return new Patient(PatientId.of(id), nom, dateNaissance, typePompe);
    }

    @Id
    public UUID getId() {
        return id;
    }

    public PatientJpaEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getNom() {
        return nom;
    }

    public PatientJpaEntity setNom(String nom) {
        this.nom = nom;
        return this;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public PatientJpaEntity setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
        return this;
    }

    @Enumerated(value = EnumType.STRING)
    public TypePompe getTypePompe() {
        return typePompe;
    }

    public PatientJpaEntity setTypePompe(TypePompe typePompe) {
        this.typePompe = typePompe;
        return this;
    }
}
