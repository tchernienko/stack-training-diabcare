package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientApplicationService {

    private final PatientRepositoryPort repositoryPort;

    @Transactional
    public void save(Patient patient) {
        repositoryPort.save(patient);
    }

    public List<Patient> findAll() {
        return repositoryPort.findAll();
    }

    public Patient findById(PatientId id) throws PatientNotFoundException {
        Optional<Patient> result = repositoryPort.findById(id);
        return result.orElseThrow(
            () -> new PatientNotFoundException(id.getValue(), "Pas de avec cet id %s".formatted(id))
        );
    }

}
