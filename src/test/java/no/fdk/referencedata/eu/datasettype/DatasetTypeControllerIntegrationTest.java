package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.datasettype.*;
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

import static no.fdk.referencedata.eu.datasettype.LocalDatasetTypeHarvester.DATASET_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class DatasetTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        DatasetTypeService datasetTypeService = new DatasetTypeService(
                new LocalDatasetTypeHarvester("1"),
                datasetTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        datasetTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_dataset_types_returns_valid_response() {
        DatasetTypes datasetTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/dataset-types", DatasetTypes.class);

        assertEquals(DATASET_TYPES_SIZE, datasetTypes.getDatasetTypes().size());

        DatasetType first = datasetTypes.getDatasetTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/APROF", first.getUri());
        assertEquals("APROF", first.getCode());
        assertEquals("Application profile", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_dataset_type_by_code_returns_valid_response() {
        DatasetType datasetType =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/dataset-types/HVD", DatasetType.class);

        assertNotNull(datasetType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/HVD", datasetType.getUri());
        assertEquals("HVD", datasetType.getCode());
        assertEquals("High-value dataset", datasetType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_dataset_types_fails_without_api_key() {
        assertEquals(DATASET_TYPES_SIZE, datasetTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DATASET_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/dataset-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(DATASET_TYPES_SIZE, datasetTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DATASET_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_dataset_types_executes_a_force_update() {
        assertEquals(DATASET_TYPES_SIZE, datasetTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DATASET_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/dataset-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DATASET_TYPES_SIZE, datasetTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DATASET_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_dataset_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/dataset-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(DatasetTypeControllerIntegrationTest.class.getClassLoader().getResource("dataset-types-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
