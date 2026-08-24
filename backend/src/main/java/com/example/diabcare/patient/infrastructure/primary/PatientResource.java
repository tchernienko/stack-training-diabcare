package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.application.PatientApplicationService;
import com.example.diabcare.patient.domain.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class PatientResource {

    private final PatientApplicationService patientApplicationService;

    @Autowired
    public PatientResource(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @Transactional
    @PostMapping("/patients")
    public ResponseEntity<RestResponsePatient> createPatient(
            @RequestBody RestRequestPatient restRequestPatient) {

        Optional<Patient> patient = Optional.ofNullable(
                RestRequestPatient.toDomain(restRequestPatient)
        );

        if (patient.isEmpty()) {
            log.info("Patient not created, invalid or empty body provided");
            return ResponseEntity.badRequest().build();
        }

        patientApplicationService.save(patient.get());

        log.info("Patient created");
        return ResponseEntity.of(
                Optional.of(
                    RestResponsePatient.of(patient.get())
                )
        );
    }


}
