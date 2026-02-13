package no.fdk.referencedata.eu.highvaluecategories;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester.HIGH_VALUE_CATEGORIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class HighValueCategoryControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HighValueCategoryRepository highValueCategoryRepository;

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

        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("2.1"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        highValueCategoryService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_high_value_categories_returns_valid_response() {
        HighValueCategories categories =
                restClient.get()
                        .uri("/eu/high-value-categories")
                        .retrieve()
                        .body(HighValueCategories.class);

        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, categories.getHighValueCategories().size());

        HighValueCategory first = categories.getHighValueCategories().get(0);
        assertEquals("http://data.europa.eu/bna/c_03ba8d92", first.getUri());
        assertEquals("c_03ba8d92", first.getCode());
        assertEquals("Regular lock and bridge operating times", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_high_value_category_by_code_returns_valid_response() {
        HighValueCategory category =
                restClient.get()
                        .uri("/eu/high-value-categories/c_a9135398")
                        .retrieve()
                        .body(HighValueCategory.class);

        assertNotNull(category);
        assertEquals("http://data.europa.eu/bna/c_a9135398", category.getUri());
        assertEquals("c_a9135398", category.getCode());
        assertEquals("Companies and company ownership", category.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_high_value_categories_fails_without_api_key() {
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, highValueCategoryRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertEquals("2.1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post()
                .uri("/eu/high-value-categories")
                .headers(h -> h.addAll(headers))
                .exchange((request, response2) -> ResponseEntity.status(response2.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, highValueCategoryRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertEquals("2.1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_high_value_categories_executes_a_force_update() {
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, highValueCategoryRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertEquals("2.1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post()
                .uri("/eu/high-value-categories")
                .headers(h -> h.addAll(headers))
                .exchange((request, response2) -> ResponseEntity.status(response2.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, highValueCategoryRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertEquals("2.1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_high_value_categories_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/high-value-categories", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(HighValueCategoryControllerIntegrationTest.class.getClassLoader().getResource("high-value-categories.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
