package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;

import java.util.List;
import java.util.Optional;

public interface PatientRepositoryPort {

    void save(Patient patient);

    List<Patient> findAll();

    Optional<Patient> findById(PatientId id);
}
