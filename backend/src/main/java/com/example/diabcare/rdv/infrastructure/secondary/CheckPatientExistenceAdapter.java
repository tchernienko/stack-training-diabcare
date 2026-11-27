package com.example.diabcare.rdv.infrastructure.secondary;

import com.example.diabcare.patient.application.PatientApplicationService;
import com.example.diabcare.rdv.application.CheckPatientExistencePort;
import com.example.diabcare.rdv.domain.PatientRef;
import org.springframework.stereotype.Component;

@Component
public class CheckPatientExistenceAdapter implements CheckPatientExistencePort {

    private final PatientApplicationService patientApplicationService;

    public CheckPatientExistenceAdapter(PatientApplicationService patientApplicationService) {
        this.patientApplicationService = patientApplicationService;
    }

    @Override
    public boolean patientExiste(PatientRef patientRef) {
        return patientApplicationService.patientExiste(patientRef.value());
    }

}
