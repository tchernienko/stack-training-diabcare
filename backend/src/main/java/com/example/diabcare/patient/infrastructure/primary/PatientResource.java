package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.application.PatientApplicationService;
import com.example.diabcare.patient.domain.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PatientResource {

    private final PatientApplicationService patientApplicationService;

    public PatientResource(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @PostMapping("/patients")
    public ResponseEntity<RestResponsePatient> createPatient(
            @RequestBody RestRequestPatient restRequestPatient) {

        Patient patient = RestRequestPatient.toDomain(restRequestPatient);

        patientApplicationService.save(patient);

        log.info("Patient created with id {}", patient.getId().getValue());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponsePatient.of(patient));
    }


}
