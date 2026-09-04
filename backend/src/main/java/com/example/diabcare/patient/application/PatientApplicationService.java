package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientApplicationService {

    private final PatientRepositoryPort repository;

    @Transactional
    public void save(Patient patient) {
        repository.save(patient);
    }


}
