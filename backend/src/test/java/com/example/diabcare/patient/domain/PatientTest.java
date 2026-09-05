package com.example.diabcare.patient.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PatientTest {

    // TODO cas à couvrir : dateNaissance = hier doit permettre la création
    //  (frontière opposée à shouldCreationFail_todayDateNaissance)

    private Patient.PatientBuilder testPatientBuilder;

    @BeforeEach
    void setUp() {
        testPatientBuilder = Patient.builder()
                .id(PatientId.newOne())
                .nom("test")
                .dateNaissance(LocalDate.of(2014, 11, 27))
                .typePompe(TypePompe.PATCH);
    }

    @Test
    void shouldCreatePatientWithAllFields() {
        UUID patientId = UUID.randomUUID();
        Patient patient = testPatientBuilder
                .id(PatientId.of(patientId))
                .build();
        assertThat(patient).isNotNull();
        assertThat(patient.getId().getValue()).isEqualTo(patientId);
        assertThat(patient.getNom()).isEqualTo("test");
        assertThat(patient.getDateNaissance()).isEqualTo(LocalDate.of(2014, 11, 27));
        assertThat(patient.getTypePompe()).isEqualTo(TypePompe.PATCH);
    }

    @Test
    void shouldCreatePatientWithAllFieldsButId() {
        Patient patient = testPatientBuilder
                .id(null)
                .build();
        assertThat(patient).isNotNull();
        assertThat(patient.getId().getValue()).isNotNull();
    }

    @Test
    void shouldCreationFail_noNom() {
        testPatientBuilder
                .nom(null);
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_blankNom() {
        testPatientBuilder
                .nom("  ");
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_emptyNom() {
        testPatientBuilder
                .nom("");
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_noDateNaissance() {
        testPatientBuilder.dateNaissance(null);
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_futureDateNaissance() {
        testPatientBuilder
                .dateNaissance(LocalDate.now().plusWeeks(1));
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_todayDateNaissance() {
        testPatientBuilder
                .dateNaissance(LocalDate.now());
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

    @Test
    void shouldCreationFail_noTypePompe() {
        testPatientBuilder
                .typePompe(null);
        assertThatThrownBy(testPatientBuilder::build).isInstanceOf(InvalidPatientParametersException.class);
    }

}