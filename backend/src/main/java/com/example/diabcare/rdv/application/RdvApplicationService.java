package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RdvApplicationService {

    private final CheckPatientExistencePort checkPatientExistencePort;

    public RdvApplicationService(CheckPatientExistencePort checkPatientExistencePort) {
        this.checkPatientExistencePort = checkPatientExistencePort;
    }

    public Rdv planifie(PatientRef patientRef, LocalDateTime dateHeure) throws RdvPatientUnknownException {
        if (checkPatientExistencePort.patientExiste(patientRef)) {
            return Rdv.planifie(patientRef, dateHeure);
        }
        throw new RdvPatientUnknownException(patientRef, "Patient with patientRef %s not found, can't create Rdv for him/her".formatted(patientRef.value()));
    }

}
