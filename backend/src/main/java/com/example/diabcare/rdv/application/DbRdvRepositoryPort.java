package com.example.diabcare.rdv.application;

import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;

import java.util.List;

public interface DbRdvRepositoryPort {

    Rdv save(Rdv rdv);

    /**
     *
     * @param patientRef the patientRef for which to search rdv
     * @return the rdv list for the given patientRef (or an empty list if does not match with a patient), sorted by dateHeure asc
     */
    List<Rdv> findByPatientRef(PatientRef patientRef);

}
