package com.example.diabcare.rdv.infrastructure.secondary;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

/**
 * spring data repository interface to generate queries without having to write sql or hql.
 */
public interface SpringJpaRdvRepository extends CrudRepository<RdvJpaEntity, UUID> {

    RdvJpaEntity save(RdvJpaEntity rdv);

    List<RdvJpaEntity> findByPatientRefOrderByDateHeure(UUID patientRef);

}
