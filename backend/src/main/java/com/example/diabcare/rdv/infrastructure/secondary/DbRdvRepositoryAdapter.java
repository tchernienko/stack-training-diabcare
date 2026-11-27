package com.example.diabcare.rdv.infrastructure.secondary;

import com.example.diabcare.rdv.application.DbRdvRepositoryPort;
import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DbRdvRepositoryAdapter implements DbRdvRepositoryPort {

    private final SpringJpaRdvRepository repository;

    public DbRdvRepositoryAdapter(SpringJpaRdvRepository repository) {
        this.repository = repository;
    }

    @Override
    public Rdv save(Rdv rdv) {
        RdvJpaEntity rdvJpaEntity = RdvJpaEntity.of(rdv);
        RdvJpaEntity savedRdvJpaEntity = repository.save(rdvJpaEntity);
        return savedRdvJpaEntity.toDomain();
    }

    @Override
    public List<Rdv> findByPatientRef(PatientRef patientRef) {
        var rdvsJpa = repository.findByPatientRefOrderByDateHeure(patientRef.value());
        return rdvsJpa.stream().map(RdvJpaEntity::toDomain).toList();
    }
}
