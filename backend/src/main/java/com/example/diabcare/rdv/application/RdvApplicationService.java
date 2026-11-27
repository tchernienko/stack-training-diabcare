package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RdvApplicationService {

    private final CheckPatientExistencePort checkPatientExistencePort;
    private final DbRdvRepositoryPort dbRdvRepositoryPort;

    public RdvApplicationService(DbRdvRepositoryPort dbRdvRepositoryPort, CheckPatientExistencePort checkPatientExistencePort) {
        this.dbRdvRepositoryPort = dbRdvRepositoryPort;
        this.checkPatientExistencePort = checkPatientExistencePort;
    }

    public Rdv planifie(PatientRef patientRef, LocalDateTime dateHeure) throws RdvPatientUnknownException {
        if (checkPatientExistencePort.patientExiste(patientRef)) {
            var rdv = Rdv.planifie(patientRef, dateHeure);
            return dbRdvRepositoryPort.save(rdv);
        }
        throw new RdvPatientUnknownException(patientRef, "Patient with patientRef %s not found, can't create Rdv for him/her".formatted(patientRef.value()));
    }

    /**
     *
     * @param patientRef the patientRef for which to search rdv
     * @return the rdv list for the given patientRef (or an empty list if does not match with a patient), sorted by dateHeure asc
     */
    public List<Rdv> findByPatientRef(PatientRef patientRef) {
        return dbRdvRepositoryPort.findByPatientRef(patientRef);
    }

}
