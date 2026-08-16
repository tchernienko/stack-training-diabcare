package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.domain.Patient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PatientResource {

    @PostMapping("/patients")
    public ResponseEntity<RestPatient> createPatient(
            @RequestBody RestPatient restPatient) {

        Optional<Patient> patient = Optional.ofNullable(RestPatient.toDomain(restPatient));

        if (patient.isEmpty()) {
            //TODO
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }


}
