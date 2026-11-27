package com.example.diabcare.rdv.infrastructure.secondary;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvId;
import com.example.diabcare.rdv.domain.RdvStatut;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class RdvJpaEntityTest {

    private final RdvId rdvId = RdvId.newOne();
    private final PatientRef patientRef = PatientRef.of(UUID.randomUUID());
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    @ParameterizedTest
    @EnumSource(value = RdvStatut.class)
    void shouldConvertRdvToJpaEntity(RdvStatut statut) {
        Rdv rdv = new Rdv.Builder().id(rdvId).patientRef(patientRef).dateHeure(yesterday).statut(statut).build();
        var result = RdvJpaEntity.of(rdv);

        assertSoftly(a -> {
            a.assertThat(result.getId()).isEqualTo(rdvId.value());
            a.assertThat(result.getPatientRef()).isEqualTo(patientRef.value());
            a.assertThat(result.getDateHeure()).isEqualTo(yesterday);
            a.assertThat(result.getRdvStatut()).isEqualTo(statut);
        });
    }

    @ParameterizedTest
    @EnumSource(value = RdvStatut.class)
    void shouldConvertJpaEntityToDomain(RdvStatut statut) {
        RdvJpaEntity jpaEntity = new RdvJpaEntity(rdvId.value(), patientRef.value(), yesterday, statut);

        Rdv result = jpaEntity.toDomain();
        assertSoftly(a -> {
            a.assertThat(result.id().value()).isEqualTo(rdvId.value());
            a.assertThat(result.patientRef().value()).isEqualTo(patientRef.value());
            a.assertThat(result.dateHeure()).isEqualTo(yesterday);
            a.assertThat(result.statut()).isEqualTo(statut);
        });
    }
}
