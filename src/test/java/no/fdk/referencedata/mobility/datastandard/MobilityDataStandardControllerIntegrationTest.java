package no.fdk.referencedata.mobility.datastandard;

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

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class MobilityDataStandardControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MobilityDataStandardRepository mobilityDataStandardRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MobilityDataStandardService mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.0.0"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityDataStandardService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_mobility_data_standards_returns_valid_response() {
        MobilityDataStandards mobilityDataStandards =
                this.restTemplate.getForObject("http://localhost:" + port + "/mobility/data-standards", MobilityDataStandards.class);

        assertEquals(15, mobilityDataStandards.getMobilityDataStandards().size());

        MobilityDataStandard first = mobilityDataStandards.getMobilityDataStandards().get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/c-its", first.getUri());
        assertEquals("c-its", first.getCode());
        assertEquals("C-ITS", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_mobility_data_standard_by_code_returns_valid_response() {
        MobilityDataStandard standard =
                this.restTemplate.getForObject("http://localhost:" + port + "/mobility/data-standards/gml", MobilityDataStandard.class);

        assertNotNull(standard);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/gml", standard.getUri());
        assertEquals("gml", standard.getCode());
        assertEquals("GML", standard.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_mobility_data_standards_fails_without_api_key() {
        assertEquals(15, mobilityDataStandardRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/mobility/data-standards",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(15, mobilityDataStandardRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_mobility_data_standards_executes_a_force_update() {
        assertEquals(15, mobilityDataStandardRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/mobility/data-standards",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15, mobilityDataStandardRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertEquals("1.1.0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_mobility_data_standards_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/mobility/data-standards", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MobilityDataStandardControllerIntegrationTest.class.getClassLoader().getResource("mobility-data-standards.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
