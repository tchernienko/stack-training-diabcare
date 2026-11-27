package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.InvalidRdvParametersException;
import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvId;
import com.example.diabcare.rdv.domain.RdvStatut;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RdvApplicationServiceTest {

    private final CheckPatientExistencePort checkPatientExistencePort = Mockito.mock(CheckPatientExistencePort.class);
    private final DbRdvRepositoryPort dbRdvRepositoryPort = Mockito.mock(DbRdvRepositoryPort.class);
    private final RdvApplicationService rdvApplicationService = new RdvApplicationService(dbRdvRepositoryPort, checkPatientExistencePort);
    private final PatientRef patientRef = PatientRef.of(UUID.randomUUID());
    private final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    @Nested
    class Planifie {

        @Test
        void shouldPlanifieFailWithUnknownPatientRef() {
            Mockito.when(checkPatientExistencePort.patientExiste(any())).thenReturn(false);
            assertThatThrownBy(
                () -> rdvApplicationService.planifie(patientRef, tomorrow)
            ).isInstanceOfSatisfying(RdvPatientUnknownException.class, e -> assertThat(e.getPatientRef()).isEqualTo(patientRef));
            verify(dbRdvRepositoryPort, never()).save(any(Rdv.class));
        }

        @Test
        void shouldPlanifieThrowWithPastDateHeureAndPatientRefExists() {
            Mockito.when(checkPatientExistencePort.patientExiste(patientRef)).thenReturn(true);
            assertThatThrownBy(
                () -> rdvApplicationService.planifie(patientRef, yesterday)
            ).isInstanceOf(InvalidRdvParametersException.class);
            verify(dbRdvRepositoryPort, never()).save(any(Rdv.class));
        }

        @Test
        void shouldPlanifieSuccess_checkReturnedRdv() {
            Rdv expected = new Rdv.Builder().id(RdvId.newOne()).dateHeure(tomorrow).patientRef(patientRef).statut(RdvStatut.PLANIFIE).build();
            Mockito.when(dbRdvRepositoryPort.save(any(Rdv.class))).thenReturn(expected);
            Mockito.when(checkPatientExistencePort.patientExiste(patientRef)).thenReturn(true);

            Rdv rdv = rdvApplicationService.planifie(patientRef, tomorrow);

            assertThat(rdv).isEqualTo(expected);
        }

        @Test
        void shouldPlanifieSuccess_checkPortCall() {
            Mockito.when(checkPatientExistencePort.patientExiste(patientRef)).thenReturn(true);
            ArgumentCaptor<Rdv> rdvCaptor = ArgumentCaptor.forClass(Rdv.class);

            //pas besoin du retour dans ce test. On veut vérifier que le service appelle le port une seule fois et que l'appel à save du port se fait avec les valeurs passées à l'appel du service.
            //pas besoin de stub sur le save du port, mockito l'appel quand même, même s'il renvoit null, c'est pas grave ici car on veut juste vérifier l'appel unique avec les bonnes valeurs.
            rdvApplicationService.planifie(patientRef, tomorrow);
            verify(dbRdvRepositoryPort).save(rdvCaptor.capture());
            var capturedRdv = rdvCaptor.getValue();

            assertSoftly(a -> {
                a.assertThat(capturedRdv.id()).isNotNull();
                a.assertThat(capturedRdv.dateHeure()).isEqualTo(tomorrow);
                a.assertThat(capturedRdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(capturedRdv.statut()).isEqualTo(RdvStatut.PLANIFIE);
            });
        }
    }

    @Nested
    class Read {

        @Test
        void shouldFindRdvByPatientRef() {
            //given
            var rdvId1 = RdvId.newOne();
            var rdvId2 = RdvId.newOne();

            var rdv1 = new Rdv(rdvId1, patientRef, tomorrow, RdvStatut.PLANIFIE);
            var rdv2 = new Rdv(rdvId2, patientRef, yesterday, RdvStatut.ANNULE);
            var rdvs = List.of(rdv1, rdv2);
            Mockito.when(dbRdvRepositoryPort.findByPatientRef(patientRef)).thenReturn(rdvs);

            //when
            var rdvsObtainedViaAppService = rdvApplicationService.findByPatientRef(patientRef);

            //then
            assertThat(rdvsObtainedViaAppService).isEqualTo(rdvs);
        }

    }

}
