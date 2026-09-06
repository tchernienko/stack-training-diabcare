package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.application.PatientRepositoryPort;
import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DbPatientRepositoryAdapter implements PatientRepositoryPort {

    private final SpringJpaPatientRepository jpaRepository;

    public DbPatientRepositoryAdapter(SpringJpaPatientRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Patient patient) {
        PatientJpaEntity entity = PatientJpaEntity.of(patient);
        jpaRepository.save(entity);
    }

    @Override
    public List<Patient> findAll() {
        Iterable<PatientJpaEntity> jpaEntities = jpaRepository.findAll();

        List<Patient> result = new ArrayList<>();
        jpaEntities.forEach((entity) -> {
            result.add(entity.toDomain());
        });
        return result;
    }

    @Override
    public Optional<Patient> findById(PatientId id) {
        Optional<PatientJpaEntity> jpaEntity = jpaRepository.findById(id.getValue());
        if (jpaEntity.isPresent()) {
            return Optional.of(jpaEntity.get().toDomain());
        }
        return Optional.empty();
    }
}
