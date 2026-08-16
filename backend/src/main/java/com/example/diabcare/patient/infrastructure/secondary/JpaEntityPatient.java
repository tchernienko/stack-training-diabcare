package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import com.example.diabcare.patient.domain.TypePompe;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class JpaEntityPatient {

    private UUID id;

    private String nom;

    private LocalDate dateNaissance;

    private TypePompe typePompe;

    @Transient
    public Patient toDomain() {
        return new Patient(PatientId.of(id), nom, dateNaissance, typePompe);
    }

    @Id
    public UUID getId() {
        return id;
    }

    public JpaEntityPatient setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getNom() {
        return nom;
    }

    public JpaEntityPatient setNom(String nom) {
        this.nom = nom;
        return this;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public JpaEntityPatient setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
        return this;
    }

    public TypePompe getTypePompe() {
        return typePompe;
    }

    public JpaEntityPatient setTypePompe(TypePompe typePompe) {
        this.typePompe = typePompe;
        return this;
    }
}
