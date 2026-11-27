package com.example.diabcare.rdv.infrastructure.primary;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RestRdvPlanifieRequest(
    @NotNull RestPatientRef patientRef,
    @NotNull LocalDateTime dateHeure) {
}
