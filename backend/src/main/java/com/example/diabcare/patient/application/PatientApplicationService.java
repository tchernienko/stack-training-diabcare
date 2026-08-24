package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientApplicationService {

    private PatientRepositoryPort repository;

    @Autowired
    public PatientApplicationService(PatientRepositoryPort repository) {
        this.repository = repository;
    }

    public void save(Patient patient) {
        repository.save(patient);
    }


}
