package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester.CONCEPT_STATUSES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ConceptStatusControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("1"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        conceptStatusService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_statuses_returns_valid_response() {
        ConceptStatuses statuses =
                restClient.get().uri("/eu/concept-statuses").retrieve().body(ConceptStatuses.class);

        assertEquals(CONCEPT_STATUSES_SIZE, statuses.getConceptStatuses().size());

        ConceptStatus first = statuses.getConceptStatuses().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", first.getUri());
        assertEquals("CANDIDATE", first.getCode());
        assertEquals("candidate", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_status_by_code_returns_valid_response() {
        ConceptStatus status =
                restClient.get().uri("/eu/concept-statuses/CURRENT").retrieve().body(ConceptStatus.class);

        assertNotNull(status);
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CURRENT", status.getUri());
        assertEquals("CURRENT", status.getCode());
        assertEquals("current", status.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_statuses_fails_without_api_key() {
        assertEquals(CONCEPT_STATUSES_SIZE, conceptStatusRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CONCEPT_STATUS.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/concept-statuses")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(CONCEPT_STATUSES_SIZE, conceptStatusRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CONCEPT_STATUS.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_statuses_executes_a_force_update() {
        assertEquals(CONCEPT_STATUSES_SIZE, conceptStatusRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CONCEPT_STATUS.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/concept-statuses")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CONCEPT_STATUSES_SIZE, conceptStatusRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CONCEPT_STATUS.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_concept_status_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/concept-statuses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ConceptStatusControllerIntegrationTest.class.getClassLoader().getResource("concept-status.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
