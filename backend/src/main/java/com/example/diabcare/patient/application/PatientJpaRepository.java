package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;

public interface PatientJpaRepository {

    void save(Patient patient);

}
