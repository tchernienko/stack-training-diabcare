package com.example.diabcare.patient.infrastructure.primary;

import com.example.diabcare.patient.application.PatientApplicationService;
import com.example.diabcare.patient.domain.Patient;
import com.example.diabcare.patient.domain.PatientId;
import com.example.diabcare.patient.domain.TypePompe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(PatientResource.class)
class PatientResourceTest {

    // TODO cas à couvrir : dateNaissance = aujourd'hui → 400 + field=dateNaissance
    //  (miroir web de PatientTest#shouldCreationFail_todayDateNaissance)

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private PatientApplicationService patientApplicationService;

    private Patient.PatientBuilder testPatientBuilder;

    @BeforeEach
    void setUp() {
        testPatientBuilder = Patient.builder()
            .id(PatientId.newOne())
            .nom("test")
            .dateNaissance(LocalDate.of(2014, 11, 27))
            .typePompe(TypePompe.PATCH);
    }

    @Test
    void shouldCreatePatient() {
        String body = """
                {"nom":"test","dateNaissance":"2014-11-27", "typePompe":"PATCH"}""";
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .hasStatus(HttpStatus.CREATED)
        .bodyJson();

        result.extractingPath("$.id").asString().isNotBlank();
        result.extractingPath("$.nom").isEqualTo("test");
        result.extractingPath("$.typePompe").isEqualTo("PATCH");
        result.extractingPath("$.dateNaissance").isEqualTo("2014-11-27");

        verify(patientApplicationService).save(any(Patient.class));

        // à garder pour ref ultérieur
//        System.out.println(objectMapper.readTree(jsonResult.actual().getJson()).toPrettyString());

    }

    @Test
    void shouldFailWithNoNom() {
        String body = """
                {"dateNaissance":"2014-11-27", "typePompe":"PATCH"}""";
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
        result.hasStatus(HttpStatus.BAD_REQUEST);
        result.bodyJson().extractingPath("$.field").isEqualTo("nom");

//        System.out.println(result.bodyJson().actual().getJson());

        verify(patientApplicationService, never()).save(any(Patient.class));

    }

    @Test
    void shouldFailWithNoDateNaissance() {
        String body = """
                {"nom": "test", "typePompe":"PATCH"}""";
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
        result.hasStatus(HttpStatus.BAD_REQUEST);
        result.bodyJson().extractingPath("$.field").isEqualTo("dateNaissance");

        verify(patientApplicationService, never()).save(any(Patient.class));

    }

    @Test
    void shouldFailWithFutureDateNaissance() {
        LocalDate futureDate = LocalDate.now().plusWeeks(1);
        String futureDateNaissance = futureDate.format(DateTimeFormatter.ISO_DATE);

        String body = """
                {"nom": "test", "dateNaissance": "%s", "typePompe":"PATCH"}""".formatted(futureDateNaissance);
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
        result.hasStatus(HttpStatus.BAD_REQUEST);
        result.bodyJson().extractingPath("$.field").isEqualTo("dateNaissance");

        verify(patientApplicationService, never()).save(any(Patient.class));

    }

    @Test
    void shouldFailWithInvalidTypePompe() {
        String body = """
                {"nom": "test", "dateNaissance": "2014-11-27", "typePompe":"PATCHOUNET"}""";
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
        result.hasStatus(HttpStatus.BAD_REQUEST);
        var json = result.bodyJson();
        json.doesNotHavePath("$.field");
        json.extractingPath("$.title").isEqualTo("Requête illisible/invalide");

        verify(patientApplicationService, never()).save(any(Patient.class));

    }

    @Test
    void shouldFailWithNoTypePompe() {
        String body = """
                {"nom": "test", "dateNaissance": "2014-11-27"}""";
        var result = assertThat(
                mockMvcTester.post()
                        .uri("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
        );
        result.hasStatus(HttpStatus.BAD_REQUEST);
        result.bodyJson().extractingPath("$.field").isEqualTo("typePompe");

        verify(patientApplicationService, never()).save(any(Patient.class));

    }

    @Test
    void shouldFindAllPatients() {

        when(patientApplicationService.findAll()).thenReturn(List.of(testPatientBuilder.build()));

        var result = assertThat(
                mockMvcTester.get().uri("/patients")
        );
        result.hasStatus(HttpStatus.OK);
        result.bodyJson().extractingPath("$").asArray().hasSize(1);
        result.bodyJson().extractingPath("$[0].nom").isEqualTo("test");


    }

}
