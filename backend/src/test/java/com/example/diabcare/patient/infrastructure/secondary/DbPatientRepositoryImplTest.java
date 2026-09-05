package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import com.example.diabcare.patient.domain.TypePompe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({DbPatientRepositoryImpl.class})
class DbPatientRepositoryImplTest {

    //TODO ajouter un cas de test pour quand la base est vide (pas de patient), doit retourner une liste vide, pas null

    @Autowired
    private DbPatientRepositoryImpl repository;

    @Autowired
    private TestEntityManager entityManager;

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
    void shouldSavePatient() {
        Patient testPatient = testPatientBuilder.build();

        repository.save(testPatient);
        entityManager.flush();
        entityManager.clear();

        PatientJpaEntity patientEntity = entityManager.find(PatientJpaEntity.class, testPatient.getId().getValue());

        assertThat(patientEntity.getId()).isEqualTo(testPatient.getId().getValue());
        assertThat(patientEntity.getNom()).isEqualTo(testPatient.getNom());
        assertThat(patientEntity.getDateNaissance()).isEqualTo(testPatient.getDateNaissance());

        //pas besoin de vérifier ça de cette façon, cf commentaire ci-dessous
        assertThat(patientEntity.getTypePompe()).isEqualTo(TypePompe.PATCH);

        //vérification que le typePompe est stocké en string car vient d'une enum. claude dit qu'il faut vérifier avec
        //une native query car le find passe par hibernate ou le cache L1 donc ne check pas le stockage réel.
        String value = (String) entityManager.getEntityManager()
                .createNativeQuery("select type_pompe from patient where id = :id", String.class)
                .setParameter("id", testPatient.getId().getValue())
                .getSingleResult();
        assertThat(value).isEqualTo("PATCH");

    }

    //TODO attention ce test utilise getFirst, penser à changer s'il faut ajouter d'autres patients à ce test.
    // suffisant pour l'instant
    @Test
    void shouldFindAllPatients() {
        Patient testPatient = testPatientBuilder.build();
        entityManager.persist(PatientJpaEntity.of(testPatient));
        entityManager.flush();
        entityManager.clear();

        List<Patient> patients = repository.findAll();
        assertThat(patients).hasSize(1);

        Patient firstPatient = patients.getFirst();

        assertThat(firstPatient.getId().getValue()).isEqualTo(testPatient.getId().getValue());
        assertThat(firstPatient.getNom()).isEqualTo(testPatient.getNom());
        assertThat(firstPatient.getDateNaissance()).isEqualTo(testPatient.getDateNaissance());
        assertThat(firstPatient.getTypePompe()).isEqualTo(TypePompe.PATCH);
    }

}
