package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.PatientRef;

/**
 * port to ask the patient domain if the given patientRef exists (before creating a Rdv for him/her)
 */
public interface CheckPatientExistencePort {

    boolean patientExiste(PatientRef patientRef);

}
