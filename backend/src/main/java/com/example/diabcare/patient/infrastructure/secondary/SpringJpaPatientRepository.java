package com.example.diabcare.patient.infrastructure.secondary;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@org.springframework.stereotype.Repository
public interface SpringJpaPatientRepository extends CrudRepository<PatientJpaEntity, UUID> {
}
