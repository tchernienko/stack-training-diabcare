package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import com.example.diabcare.patient.domain.TypePompe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientApplicationServiceTest {

    @InjectMocks
    private PatientApplicationService service;

    private Patient.PatientBuilder testPatientBuilder;

    @Mock
    private PatientRepositoryPort repositoryPort;

    @BeforeEach
    void setUp() {
        testPatientBuilder = Patient.builder()
            .id(PatientId.newOne())
            .nom("test")
            .dateNaissance(LocalDate.of(2014, 11, 27))
            .typePompe(TypePompe.PATCH);
    }

    @Test
    void shouldReturnPatientWhenFound() {
        Patient testPatient = testPatientBuilder.build();
        when(repositoryPort.findById(testPatient.getId())).thenReturn(Optional.of(testPatient));
        Patient result = service.findById(testPatient.getId());
        assertThat(result).isEqualTo(testPatient);
    }

    @Test
    void shouldThrowPatientNotFoundException() {
        PatientId unknownId = PatientId.newOne();
        when(repositoryPort.findById(unknownId)).thenReturn(Optional.empty());
        assertThatThrownBy(
            () -> service.findById(unknownId)
        ).isInstanceOf(PatientNotFoundException.class);
    }

}
