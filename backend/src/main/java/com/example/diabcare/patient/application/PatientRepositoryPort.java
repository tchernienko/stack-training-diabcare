package com.example.diabcare.patient.application;

import com.example.diabcare.patient.domain.Patient;

import java.util.List;

public interface PatientRepositoryPort {

    void save(Patient patient);

    List<Patient> findAll();

}
