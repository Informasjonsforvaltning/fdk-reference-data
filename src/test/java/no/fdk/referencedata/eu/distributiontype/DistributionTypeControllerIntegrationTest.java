package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
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

import static no.fdk.referencedata.eu.distributiontype.LocalDistributionTypeHarvester.DISTRIBUTION_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class DistributionTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        DistributionTypeService distributionTypeService = new DistributionTypeService(
                new LocalDistributionTypeHarvester("1"),
                distributionTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        distributionTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_distribution_types_returns_valid_response() {
        DistributionTypes distributionTypes =
                restClient.get().uri("/eu/distribution-types").retrieve().body(DistributionTypes.class);

        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypes.getDistributionTypes().size());

        DistributionType first = distributionTypes.getDistributionTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", first.getUri());
        assertEquals("DOWNLOADABLE_FILE", first.getCode());
        assertEquals("Downloadable file", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_distribution_type_by_code_returns_valid_response() {
        DistributionType distributionType =
                restClient.get().uri("/eu/distribution-types/DOWNLOADABLE_FILE").retrieve().body(DistributionType.class);

        assertNotNull(distributionType);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", distributionType.getUri());
        assertEquals("DOWNLOADABLE_FILE", distributionType.getCode());
        assertEquals("Downloadable file", distributionType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_distribution_types_fails_without_api_key() {
        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DISTRIBUTION_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/distribution-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DISTRIBUTION_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_distribution_types_executes_a_force_update() {
        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DISTRIBUTION_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/distribution-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DISTRIBUTION_TYPES_SIZE, distributionTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DISTRIBUTION_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_distribution_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/distribution-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(DistributionTypeControllerIntegrationTest.class.getClassLoader().getResource("distribution-types-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
