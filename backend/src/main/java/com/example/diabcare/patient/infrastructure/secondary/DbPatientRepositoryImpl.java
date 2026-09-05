package com.example.diabcare.patient.infrastructure.secondary;

import com.example.diabcare.patient.application.PatientRepositoryPort;
import com.example.diabcare.patient.domain.Patient;
import org.springframework.stereotype.Component;

@Component
public class DbPatientRepositoryImpl implements PatientRepositoryPort {

    private final SpringJpaPatientRepository repository;

    public DbPatientRepositoryImpl(SpringJpaPatientRepository repository) {
        this.repository = repository;
    }

    public void save(Patient patient) {
        PatientJpaEntity entity = PatientJpaEntity.of(patient);
        repository.save(entity);
    }

}
