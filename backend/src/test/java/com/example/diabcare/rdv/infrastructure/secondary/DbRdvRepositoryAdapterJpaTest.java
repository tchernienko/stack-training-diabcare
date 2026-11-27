package com.example.diabcare.rdv.infrastructure.secondary;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvId;
import com.example.diabcare.rdv.domain.RdvStatut;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
@Import(DbRdvRepositoryAdapter.class)
public class DbRdvRepositoryAdapterJpaTest {

    private final PatientRef patientRef = PatientRef.of(UUID.randomUUID());
    private final PatientRef otherPatientRef = PatientRef.of(UUID.randomUUID());
    private final RdvId rdvId = RdvId.newOne();
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1).truncatedTo(ChronoUnit.MINUTES);
    private final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES);
    private final LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2).truncatedTo(ChronoUnit.MINUTES);

    @Autowired
    private DbRdvRepositoryAdapter adapter;

    @Autowired
    private EntityManager testEM;

    @Nested
    class Persistance {

        @EnumSource(value = RdvStatut.class)
        @ParameterizedTest
        void shouldPersistRdvWithVariousStatut(RdvStatut statut) {
            //pas de clé étrangère de rdv vers patient, et le domaine rdv ne connait pas le domaine patient, donc pas besoin de patient en base.
            Rdv rdvToPersist = new Rdv.Builder().id(rdvId).patientRef(patientRef).dateHeure(yesterday).statut(statut).build();
            Rdv savedRdv = adapter.save(rdvToPersist);
            testEM.flush();
            testEM.clear();

            //verification en sql natif pour ne pas passer par le mapping, qui pourrait avoir des erreurs. les casts sont nécessaires car les uuid ne sont pas stockés en string
            Object[] result = (Object[]) testEM.createNativeQuery("select cast(id as varchar), cast(patient_ref as varchar), date_heure, statut from rdv where id=:id")
                .setParameter("id", rdvId.value().toString())
                .getSingleResult();
            assertSoftly(a -> {
                a.assertThat(savedRdv).isEqualTo(rdvToPersist);
                a.assertThat(result[0]).isEqualTo(rdvId.value().toString());
                a.assertThat(result[1]).isEqualTo(patientRef.value().toString());
                a.assertThat((LocalDateTime)result[2]).isEqualTo(yesterday);
                a.assertThat(result[3]).isEqualTo(statut.name());
            });
        }
    }

    @Nested
    class Read {

        @Test
        void shouldReturnEmptyListForPatientWithoutRdv() {
            populateRdvs();
            var result = adapter.findByPatientRef(PatientRef.of(UUID.randomUUID()));
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnRdvsMatchingPatientRefSortedByDateHeure() {
            populateRdvs();
            var result = adapter.findByPatientRef(patientRef);
            assertThat(result).hasSize(2);
            assertSoftly(a -> {
                var zero = result.get(0);
                var one = result.get(1);
                a.assertThat(zero.dateHeure()).isEqualTo(twoMonthsAgo);
                a.assertThat(zero.statut()).isEqualTo(RdvStatut.HONORE);
                a.assertThat(zero.patientRef()).isEqualTo(patientRef);
                a.assertThat(one.dateHeure()).isEqualTo(tomorrow);
                a.assertThat(one.statut()).isEqualTo(RdvStatut.PLANIFIE);
                a.assertThat(one.patientRef()).isEqualTo(patientRef);
            });
        }

        private void populateRdvs() {
            Query q = testEM.createNativeQuery("insert into rdv(id, patient_ref, date_heure, statut) values(:id, :patientRef, :dateHeure, :statut)");
            var rdvs = List.of(
                new Rdv(RdvId.newOne(), patientRef, tomorrow, RdvStatut.PLANIFIE),
                new Rdv(RdvId.newOne(), patientRef, twoMonthsAgo, RdvStatut.HONORE),
                new Rdv(RdvId.newOne(), otherPatientRef, yesterday, RdvStatut.PLANIFIE)
            );
            rdvs.forEach(
              r -> q
                    .setParameter("id", r.id().value().toString())
                    .setParameter("patientRef", r.patientRef().value().toString())
                    .setParameter("dateHeure", r.dateHeure())
                    .setParameter("statut", r.statut().toString())
                  .executeUpdate()
            );
        }

    }

}
