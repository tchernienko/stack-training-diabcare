package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.application.PatientApplicationService;
import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/patients")
public class PatientResource {

    private final PatientApplicationService patientApplicationService;

    public PatientResource(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @PostMapping
    public ResponseEntity<RestResponsePatient> createPatient(
            @RequestBody RestRequestPatient restRequestPatient) {

        Patient patient = RestRequestPatient.toDomain(restRequestPatient);

        patientApplicationService.save(patient);

        log.info("Patient created with id {}", patient.getId().getValue());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestResponsePatient.of(patient));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RestResponsePatient>> getPatients() {
        //TODO gerer la pagination? le tri?
        List<Patient> patients = patientApplicationService.findAll();

        List<RestResponsePatient> result = new ArrayList<>(patients.size());

        patients.forEach((patient) -> {
            result.add(RestResponsePatient.of(patient));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping(path = "/{id}",  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponsePatient> getPatientById(@PathVariable UUID id) {
        var result = patientApplicationService.findById(PatientId.of(id));
        return ResponseEntity.status(HttpStatus.OK).body(RestResponsePatient.of(result));
    }

}
