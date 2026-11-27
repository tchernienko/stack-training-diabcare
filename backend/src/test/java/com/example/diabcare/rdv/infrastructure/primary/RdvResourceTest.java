package com.example.diabcare.rdv.infrastructure.primary;

import com.example.diabcare.rdv.application.RdvApplicationService;
import com.example.diabcare.rdv.application.RdvPatientUnknownException;
import com.example.diabcare.rdv.domain.InvalidRdvParametersException;
import com.example.diabcare.rdv.domain.PatientRef;
import com.example.diabcare.rdv.domain.Rdv;
import com.example.diabcare.rdv.domain.RdvId;
import com.example.diabcare.rdv.domain.RdvStatut;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(RdvResource.class)
class RdvResourceTest {

    private final LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).truncatedTo(ChronoUnit.MINUTES);
    private final LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withMinute(0).withSecond(0).truncatedTo(ChronoUnit.MINUTES);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private RdvApplicationService rdvApplicationService;

    @Nested
    class Read {

        @Test
        void shouldFindAllRdvsForGivenPatientRef() {
            //given
            RdvId rdvId = RdvId.newOne();
            PatientRef patientRef = PatientRef.of(UUID.randomUUID());
            Rdv rdv = new Rdv.Builder().id(rdvId).patientRef(patientRef).dateHeure(tomorrow).statut(RdvStatut.PLANIFIE).build();
            when(rdvApplicationService.findByPatientRef(patientRef)).thenReturn(List.of(rdv));

            //when
            var result = assertThat(
                mockMvcTester.get()
                    .uri("/api/rdv")
                    .queryParam("patientRef", patientRef.value().toString())
            );

            //then
            result.hasStatus(HttpStatus.OK);
            result.bodyJson().extractingPath("$").asArray().hasSize(1);
            //System.out.println(result.bodyJson().actual());
            var firstRdv = result.bodyJson().extractingPath("$[0]");
            //id at the root level, like other properties
            firstRdv.extracting("id").isEqualTo(rdvId.value().toString());
            firstRdv.extracting("patientRef").isEqualTo(patientRef.value().toString());
            firstRdv.extracting("statut").isEqualTo(RdvStatut.PLANIFIE.name());
            firstRdv.extracting("dateHeure").isEqualTo(formatter.format(tomorrow));
        }

        @Test
        void shouldReturnEmptyListAndHttp200WhenNoRdvFound() {
            //given
            when(rdvApplicationService.findByPatientRef(any(PatientRef.class))).thenReturn(List.of());

            //when
            var result = assertThat(
                mockMvcTester.get()
                    .uri("/api/rdv")
                    .queryParam("patientRef", UUID.randomUUID().toString())
            );

            //then
            result.hasStatus(HttpStatus.OK);
            result.bodyJson().extractingPath("$").asArray().isEmpty();
        }

        @Test
        void shouldReturn400WhenNoPatientRefProvided() {
            //when(rdvApplicationService.findByPatientRef(null)).thenThrow();
        }

    }

    @Nested
    class Create {
        @Test
        void shouldCreateRdvWithStatutPlanifieAndGivenDateForGivenPatientRef() {
            PatientRef patientRef = PatientRef.of(UUID.randomUUID());

            var body = requestBody(patientRef, formatter.format(tomorrow));

            Rdv rdv = Rdv.planifie(patientRef, tomorrow);
            when(rdvApplicationService.planifie(patientRef, tomorrow)).thenReturn(rdv);
            RdvId rdvId = rdv.id();
            var mvcTestResult = assertThat(mockMvcTester.post()
                .uri("/api/rdv").contentType(MediaType.APPLICATION_JSON).content(body)
                .exchange());
            mvcTestResult.hasStatus(HttpStatus.CREATED);

            var restResponseRdv = mvcTestResult.bodyJson().extractingPath("$");
            restResponseRdv.extracting("id").isEqualTo(rdvId.value().toString());
            restResponseRdv.extracting("patientRef").isEqualTo(patientRef.value().toString());
            restResponseRdv.extracting("dateHeure").isEqualTo(formatter.format(tomorrow));
            restResponseRdv.extracting("statut").isEqualTo(RdvStatut.PLANIFIE.name());

            mvcTestResult.headers()
                .hasValue(HttpHeaders.LOCATION, "http://localhost/api/rdv/" + rdvId.value())
                .hasValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            //the target location does not exist yet, so keep the test on it for later reference/need
        }

        @Test
        void shouldExceptionHandlerConvertRdvPatientUnknownExceptionToHttp400() {
            PatientRef patientRef = PatientRef.of(UUID.randomUUID());
            when(rdvApplicationService.planifie(any(PatientRef.class), any(LocalDateTime.class)))
                .thenThrow(new RdvPatientUnknownException(patientRef, "unknown patientRef") );

            var body = requestBody(patientRef, formatter.format(tomorrow));

            var mvcTestResult = assertThat(mockMvcTester.post()
                .uri("/api/rdv").contentType(MediaType.APPLICATION_JSON).content(body)
                .exchange());
            mvcTestResult.hasStatus(HttpStatus.BAD_REQUEST);

            //check problem detail content-type
            mvcTestResult.headers().hasValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            //check problem detail body
            var jsonBody = mvcTestResult.bodyJson();
            jsonBody.extractingPath("$.title").isEqualTo("Unknown patient");
            jsonBody.extractingPath("$.detail").isEqualTo("No patient with that id exists: %s".formatted(patientRef.value().toString()));
            jsonBody.extractingPath("$.patientRef").isEqualTo(patientRef.value().toString());

        }

        @Test
        void shouldExceptionHandlerConvertInvalidRdvParametersExceptionToHttp400() {
            var exceptionMsg = "Rdv status is planifie and dateHeure is in the past. A planifie Rdv cannot be in the past.";
            PatientRef patientRef = PatientRef.of(UUID.randomUUID());
            when(rdvApplicationService.planifie(any(PatientRef.class), any(LocalDateTime.class)))
                .thenThrow(new InvalidRdvParametersException(exceptionMsg) );

            var body = requestBody(patientRef, formatter.format(yesterday));

            var mvcTestResult = assertThat(mockMvcTester.post()
                .uri("/api/rdv").contentType(MediaType.APPLICATION_JSON).content(body)
                .exchange());
            mvcTestResult.hasStatus(HttpStatus.BAD_REQUEST);

            //check problem detail content-type
            mvcTestResult.headers().hasValue(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);

            //check problem detail body
            var jsonBody = mvcTestResult.bodyJson();
            jsonBody.extractingPath("$.title").isEqualTo("Bad Rdv parameters");
            jsonBody.extractingPath("$.detail").isEqualTo(exceptionMsg);
        }

        @Test
        void shouldMissingPatientRefReturnHttp400() {
            var body = """
                {"dateHeure": "2026-11-20T17:55:33"}
                """;
            var mvcTestResult = assertThat(mockMvcTester.post()
                .uri("/api/rdv").contentType(MediaType.APPLICATION_JSON).content(body)
                .exchange());
            mvcTestResult.hasStatus(HttpStatus.BAD_REQUEST);

            verify(rdvApplicationService, never()).planifie(any(PatientRef.class), any(LocalDateTime.class));
            //System.out.println(mvcTestResult.bodyJson().actual().getJson());
        }

        @Test
        void shouldMalformedUuidReturnHttp400() {
            var body = """
                {
                "patientRef": "abcd-1234",
                "dateHeure": "2026-11-20T17:55:33"
                }
                """;
            var mvcTestResult = assertThat(mockMvcTester.post()
                .uri("/api/rdv").contentType(MediaType.APPLICATION_JSON).content(body)
                .exchange());
            mvcTestResult.hasStatus(HttpStatus.BAD_REQUEST);

            verify(rdvApplicationService, never()).planifie(any(PatientRef.class), any(LocalDateTime.class));
        }

        private String requestBody(PatientRef patientRef, String dateHeure) {
            return """
                {
                    "patientRef": "%s",
                    "dateHeure": "%s"
                }""".formatted(patientRef.value().toString(), dateHeure);
        }


    }

}
