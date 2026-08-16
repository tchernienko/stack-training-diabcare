package com.example.diabcare.shared;

import java.util.Objects;
import java.util.UUID;

/**
 * Classe de base pour les IDs métier typés (PatientId, RendezVousId, ...).
 * Évite de faire circuler des String ou des UUID nus dans les signatures de use cases.
 */
public abstract class Identifiant {

    private final UUID valeur;

    protected Identifiant(UUID valeur) {
        this.valeur = Objects.requireNonNull(valeur, "L'identifiant ne peut pas être nul");
    }

    protected Identifiant(String valeur) {
        this(UUID.fromString(valeur));
    }

    public UUID valeur() {
        return valeur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return valeur.equals(((Identifiant) o).valeur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valeur);
    }

    @Override
    public String toString() {
        return valeur.toString();
    }
}
