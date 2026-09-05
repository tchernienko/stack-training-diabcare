package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.application.PatientRepositoryPort;
import com.example.diabcare.patient.domain.Patient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//TODO renommer en DbPatientRepositoryAdapter pour matcher avec le nom "port and adapter"?
@Component
public class DbPatientRepositoryImpl implements PatientRepositoryPort {

    private final SpringJpaPatientRepository repository;

    public DbPatientRepositoryImpl(SpringJpaPatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Patient patient) {
        PatientJpaEntity entity = PatientJpaEntity.of(patient);
        repository.save(entity);
    }

    @Override
    public List<Patient> findAll() {
        Iterable<PatientJpaEntity> jpaEntities = repository.findAll();

        List<Patient> result = new ArrayList<>();
        jpaEntities.forEach((entity) -> {
            result.add(entity.toDomain());
        });
        return result;
    }
}
