package com.example.diabcare.rdv.infrastructure.primary;

import com.example.diabcare.rdv.application.RdvApplicationService;
import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/rdv", produces = MediaType.APPLICATION_JSON_VALUE)
public class RdvResource {

    private final RdvApplicationService rdvApplicationService;

    public RdvResource(RdvApplicationService rdvApplicationService) {
        this.rdvApplicationService = rdvApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<RestResponseRdv>> getRdvByPatientRef(@RequestParam(name = "patientRef") UUID patientRef) {
        List<Rdv> rdvs = rdvApplicationService.findByPatientRef(PatientRef.of(patientRef));

        //no need to check for empty rdvs since stream().map of an empty list returns an empty list

        var restRdvs = rdvs.stream().map(RestResponseRdv::of).toList();
        return ResponseEntity.ok(restRdvs);
    }

    //TODO ajouter un GET sur /rdv/{rdvId}

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseRdv> planifie(@Valid @RequestBody RestRdvPlanifieRequest rdvPlanifieRequest) {
        Rdv rdv = rdvApplicationService.planifie(PatientRef.of(rdvPlanifieRequest.patientRef().value()), rdvPlanifieRequest.dateHeure());
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(rdv.id().value())
            .toUri();
        return ResponseEntity.created(location).body(RestResponseRdv.of(rdv));
    }

}
