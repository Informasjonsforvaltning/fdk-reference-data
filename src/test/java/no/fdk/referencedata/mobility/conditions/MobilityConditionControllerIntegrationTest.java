package no.fdk.referencedata.mobility.conditions;

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
public class MobilityConditionControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MobilityConditionRepository mobilityConditionRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.0.0"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityConditionService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_mobility_conditions_returns_valid_response() {
        MobilityConditions mobilityConditions =
                this.restTemplate.getForObject("http://localhost:" + port + "/mobility/conditions-for-access-and-usage", MobilityConditions.class);

        assertEquals(10, mobilityConditions.getMobilityConditions().size());

        MobilityCondition first = mobilityConditions.getMobilityConditions().get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/contractual-arrangement", first.getUri());
        assertEquals("contractual-arrangement", first.getCode());
        assertEquals("Contractual arrangement", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_mobility_condition_by_code_returns_valid_response() {
        MobilityCondition condition =
                this.restTemplate.getForObject("http://localhost:" + port + "/mobility/conditions-for-access-and-usage/other", MobilityCondition.class);

        assertNotNull(condition);
        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/other", condition.getUri());
        assertEquals("other", condition.getCode());
        assertEquals("Other", condition.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_mobility_conditions_fails_without_api_key() {
        assertEquals(10, mobilityConditionRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_CONDITION.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/mobility/conditions-for-access-and-usage",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(10, mobilityConditionRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_CONDITION.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_mobility_conditions_executes_a_force_update() {
        assertEquals(10, mobilityConditionRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_CONDITION.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/mobility/conditions-for-access-and-usage",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10, mobilityConditionRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_CONDITION.name()).orElseThrow();
        assertEquals("1.1.0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_mobility_conditions_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/mobility/conditions-for-access-and-usage", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MobilityConditionControllerIntegrationTest.class.getClassLoader().getResource("mobility-conditions.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
