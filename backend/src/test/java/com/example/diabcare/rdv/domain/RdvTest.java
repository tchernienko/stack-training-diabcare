package com.example.diabcare.rdv.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class RdvTest {

    private final LocalDateTime todayIn3Hours = LocalDateTime.now().plusHours(3);
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    private final PatientRef patientRef =  PatientRef.of(UUID.randomUUID());

    @Nested
    class Reconstitution {

        @Test
        void shouldPassWithAllFields_usingRdvIdOf() {
            var generatedRdvId = UUID.randomUUID();
            var rdv = new Rdv(RdvId.of(generatedRdvId), patientRef, todayIn3Hours, RdvStatut.PLANIFIE);
            assertSoftly(a -> {
                a.assertThat(rdv.id().value()).isEqualTo(generatedRdvId);
                a.assertThat(rdv.statut()).isEqualTo(RdvStatut.PLANIFIE);
                a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(rdv.dateHeure()).isEqualTo(todayIn3Hours);
            });
        }

        @Test
        void shouldPassWithPastDateHeureAndStatutPlanifie() {
            var rdv = new Rdv(RdvId.newOne(), patientRef, yesterday, RdvStatut.PLANIFIE);
            assertSoftly(a -> {
                    a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                    a.assertThat(rdv.dateHeure()).isEqualTo(yesterday);
                    a.assertThat(rdv.statut()).isEqualTo(RdvStatut.PLANIFIE);
                }
            );
        }

        @Test
        void shouldFailWithNullId() {
            assertThatThrownBy(
                () -> new Rdv(null, patientRef, todayIn3Hours, RdvStatut.PLANIFIE)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldFailWithNullPatientRef() {
            assertThatThrownBy(
                () -> new Rdv(RdvId.newOne(), null, todayIn3Hours, RdvStatut.PLANIFIE)
            ).isInstanceOf(InvalidRdvParametersException.class);
        }

        @Test
        void shouldFailWithNullDateHeure() {
            assertThatThrownBy(
                () -> new Rdv(RdvId.newOne(), patientRef, null, RdvStatut.PLANIFIE)
            ).isInstanceOf(InvalidRdvParametersException.class);
        }

        @Test
        void shouldFailWithNullRdvStatut() {
            assertThatThrownBy(
                () -> new Rdv(RdvId.newOne(), patientRef, todayIn3Hours, null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldPassWithStatusHonoreAndDateHeurePast() {
            var rdv = new Rdv(RdvId.newOne(), patientRef, yesterday, RdvStatut.HONORE);
            assertSoftly(a -> {
                a.assertThat(rdv.statut()).isEqualTo(RdvStatut.HONORE);
                a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(rdv.dateHeure()).isEqualTo(yesterday);
            });
        }

        @Test
        void shouldPassWithStatusAnnuleWithDateHeureFuture() {
            var rdv = new Rdv(RdvId.newOne(), patientRef, todayIn3Hours, RdvStatut.ANNULE);
            assertSoftly(a -> {
                a.assertThat(rdv.statut()).isEqualTo(RdvStatut.ANNULE);
                a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(rdv.dateHeure()).isEqualTo(todayIn3Hours);
            });
        }

        @Test
        void shouldPassWithStatusAnnuleWithDateHeurePast() {
            var rdv = new Rdv(RdvId.newOne(), patientRef, yesterday, RdvStatut.ANNULE);
            assertSoftly(a -> {
                a.assertThat(rdv.statut()).isEqualTo(RdvStatut.ANNULE);
                a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(rdv.dateHeure()).isEqualTo(yesterday);
            });
        }

        @Test
        void shouldFailWithStatusHonoreAndDateHeureFuture() {
            assertThatThrownBy(
                () -> new Rdv(RdvId.newOne(), patientRef, todayIn3Hours, RdvStatut.HONORE)
            ).isInstanceOf(InvalidRdvParametersException.class);
        }

    }

    @Nested
    class CreationForPlanification {

        @Test
        void shouldPassWithAllFieldsButId() {
            var rdv = Rdv.planifie(patientRef, todayIn3Hours);
            assertSoftly(a -> {
                a.assertThat(rdv.statut()).isEqualTo(RdvStatut.PLANIFIE);
                a.assertThat(rdv.patientRef()).isEqualTo(patientRef);
                a.assertThat(rdv.dateHeure()).isEqualTo(todayIn3Hours);
            });
        }

        @Test
        void shouldGenerateDifferentRdvIdsOnSuccessiveCalls() {
            var rdv1 = Rdv.planifie(patientRef, todayIn3Hours);
            var rdv2  =  Rdv.planifie(patientRef, todayIn3Hours);
            assertSoftly(a -> {
                a.assertThat(rdv1).isNotNull();
                a.assertThat(rdv1.id()).isNotNull();
                a.assertThat(rdv2).isNotNull();
                a.assertThat(rdv2.id()).isNotNull();
                a.assertThat(rdv1.id().value()).isNotEqualTo(rdv2.id().value());
            });
        }

        @Test
        void shouldFailWithStatusPlanifieAndDateHeurePast() {
            assertThatThrownBy(
                () -> Rdv.planifie(patientRef, yesterday)
            ).isInstanceOf(InvalidRdvParametersException.class);
        }

    }

}
