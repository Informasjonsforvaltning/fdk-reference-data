package no.fdk.referencedata.eu.plannedavailability;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.plannedavailability.LocalPlannedAvailabilityHarvester.PLANNED_AVAILABILITY_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class PlannedAvailabilityControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PlannedAvailabilityRepository plannedAvailabilityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        PlannedAvailabilityService plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("1"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        plannedAvailabilityService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_planned_availabilities_returns_valid_response() {
        PlannedAvailabilities plannedAvailabilities =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/planned-availabilities", PlannedAvailabilities.class);

        assertEquals(PLANNED_AVAILABILITY_SIZE, plannedAvailabilities.getPlannedAvailabilities().size());

        PlannedAvailability first = plannedAvailabilities.getPlannedAvailabilities().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/AVAILABLE", first.getUri());
        assertEquals("AVAILABLE", first.getCode());
        assertEquals("available", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_planned_availability_by_code_returns_valid_response() {
        PlannedAvailability plannedAvailability =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/planned-availabilities/TEMPORARY", PlannedAvailability.class);

        assertNotNull(plannedAvailability);
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/TEMPORARY", plannedAvailability.getUri());
        assertEquals("TEMPORARY", plannedAvailability.getCode());
        assertEquals("midlertidig", plannedAvailability.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_post_planned_availabilities_fails_without_api_key() {
        assertEquals(PLANNED_AVAILABILITY_SIZE, plannedAvailabilityRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.PLANNED_AVAILABILITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/planned-availabilities",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(PLANNED_AVAILABILITY_SIZE, plannedAvailabilityRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.PLANNED_AVAILABILITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_planned_availabilities_executes_a_force_update() {
        assertEquals(PLANNED_AVAILABILITY_SIZE, plannedAvailabilityRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.PLANNED_AVAILABILITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/planned-availabilities",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PLANNED_AVAILABILITY_SIZE, plannedAvailabilityRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.PLANNED_AVAILABILITY.name()).orElseThrow();
        assertEquals("20220715-0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_planned_availabilities_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/planned-availabilities", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(PlannedAvailabilityControllerIntegrationTest.class.getClassLoader().getResource("planned-availability-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
