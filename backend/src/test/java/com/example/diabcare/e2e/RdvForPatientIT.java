package com.example.diabcare.e2e;

import com.example.diabcare.rdv.domain.RdvStatut;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

/**
 * Test class crossing Patient and Rdv domains, pseudo end-to-end tests since it runs against a local H2 for now.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RdvForPatientIT {

    @Autowired
    private MockMvcTester mockMvcTester;

    @Test
    void shouldCreatePatientAndAssignHimRdvsAndListThoseDateAsc() {
        var stringUuidPatient = createAndCheckPatient("Bob L'éponge", "2014-11-27", "PATCH");

        //create 2 rdv with different dates to allow testing the sorting
        var secondRdv = createAndCheckRdv(stringUuidPatient, "2030-10-13T10:30:00");
        var firstRdv = createAndCheckRdv(stringUuidPatient, "2029-09-19T09:00:00");

        //get the list of rdv for the patient
        var request = mockMvcTester.get()
            .uri("/api/rdv")
            .queryParam("patientRef", stringUuidPatient);

        var result = request.exchange();
        result.assertThat().hasStatus(HttpStatus.OK.value());
        var bodyJson = result.assertThat().bodyJson();
        bodyJson.extractingPath("$").asArray().hasSize(2);

        var rdv1 = bodyJson.extractingPath("$[0]");
        var rdv2 = bodyJson.extractingPath("$[1]");
        rdv1.extracting("dateHeure").isEqualTo("2029-09-19T09:00:00");
        rdv1.extracting("id").isEqualTo(firstRdv);

        rdv2.extracting("dateHeure").isEqualTo("2030-10-13T10:30:00");
        rdv2.extracting("id").isEqualTo(secondRdv);
    }

    @Test
    void shouldFailWithHttp400WhileCreatingRdvForUnknownPatient() {
        var result = executeCreateRdvRequest(UUID.randomUUID().toString(), "2033-01-02T09:09:09");
        result.assertThat().hasStatus(HttpStatus.BAD_REQUEST.value());
        result.assertThat().bodyJson().extractingPath("$.title").isEqualTo("Unknown patient");
    }

    /**
     *
     * @return the string representation of the patientId (uuid.value)
     */
    private String createAndCheckPatient(String nom, String dateNaissance, String typePompe) {
        //create the patient
        var request = mockMvcTester.post()
            .uri("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "dateNaissance": "%s",
                    "nom": "%s",
                    "typePompe": "%s"
                }""".strip().formatted(dateNaissance, nom, typePompe));
        var result = request.exchange();
        result.assertThat().hasStatus(HttpStatus.CREATED.value());

        //checks
        var bodyJson = result.assertThat().bodyJson();
        bodyJson.extractingPath("$.id").asString().isNotBlank();
        bodyJson.extractingPath("$.nom").isEqualTo(nom);
        bodyJson.extractingPath("$.typePompe").isEqualTo(typePompe);
        bodyJson.extractingPath("$.dateNaissance").isEqualTo(dateNaissance);

        return bodyJson.extractingPath("$.id").asString().actual();
    }

    /**
     *
     * @param stringUuidPatient the patient id to create a rdv for
     * @return the string representation of the rdvId of the created rdv
     */
    private String createAndCheckRdv(String stringUuidPatient, String dateHeure) {
        var result = executeCreateRdvRequest(stringUuidPatient, dateHeure);
        result.assertThat().hasStatus(HttpStatus.CREATED.value());

        var bodyJson = result.assertThat().bodyJson();
        bodyJson.extractingPath("$.id").isNotNull();
        bodyJson.extractingPath("$.patientRef").isEqualTo(stringUuidPatient);
        bodyJson.extractingPath("$.dateHeure").isEqualTo(dateHeure);
        bodyJson.extractingPath("$.statut").isEqualTo(RdvStatut.PLANIFIE.name());

        return bodyJson.extractingPath("$.id").asString().actual();
    }

    private MvcTestResult executeCreateRdvRequest(String patientRef, String dateHeure) {
        var request = mockMvcTester.post()
            .uri("/api/rdv")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "dateHeure": "%s",
                    "patientRef": "%s"
                }""".strip().formatted(dateHeure, patientRef));
        return request.exchange();
    }

}
