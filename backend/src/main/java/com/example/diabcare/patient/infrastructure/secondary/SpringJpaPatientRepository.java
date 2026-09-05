package com.example.diabcare.patient.infrastructure.secondary;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SpringJpaPatientRepository extends CrudRepository<PatientJpaEntity, UUID> {
}
