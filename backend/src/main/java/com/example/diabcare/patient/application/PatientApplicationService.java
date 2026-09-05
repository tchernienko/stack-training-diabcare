package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientApplicationService {

    private final PatientRepositoryPort repository;

    @Transactional
    public void save(Patient patient) {
        repository.save(patient);
    }

    public List<Patient> findAll() {
        return repository.findAll();
    }


}
