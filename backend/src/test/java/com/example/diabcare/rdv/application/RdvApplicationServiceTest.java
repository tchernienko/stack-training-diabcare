package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.InvalidRdvParametersException;
import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;

class RdvApplicationServiceTest {

    private final CheckPatientExistencePort checkPatientExistencePort = Mockito.mock(CheckPatientExistencePort.class);
    private final RdvApplicationService rdvApplicationService = new RdvApplicationService(checkPatientExistencePort);
    private final PatientRef patientRef = PatientRef.of(UUID.randomUUID());
    private final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    @Test
    void shouldPlanifieFailWithUnknownPatientRef() {
        Mockito.when(checkPatientExistencePort.patientExiste(any())).thenReturn(false);
        assertThatThrownBy(
            () -> rdvApplicationService.planifie(patientRef, tomorrow)
        )
            .isInstanceOfSatisfying(RdvPatientUnknownException.class, e -> assertThat(e.getPatientRef()).isEqualTo(patientRef));
    }

    @Test
    void shouldPlanifieThrowWithPastDateHeureAndPatientRefExists() {
        Mockito.when(checkPatientExistencePort.patientExiste(patientRef)).thenReturn(true);
        assertThatThrownBy(
            () -> rdvApplicationService.planifie(patientRef, yesterday))
            .isInstanceOf(InvalidRdvParametersException.class);
    }

    @Test
    void shouldPlanifieSuccess() {
        Mockito.when(checkPatientExistencePort.patientExiste(patientRef)).thenReturn(true);
        Rdv rdv = rdvApplicationService.planifie(patientRef, tomorrow);
        assertSoftly(a -> {
            a.assertThat(rdv).isNotNull();
            a.assertThat(rdv.dateHeure()).isEqualTo(tomorrow);
            a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
        });

    }

}
