package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.eurovoc.EuroVocControllerIntegrationTest;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester.DATA_THEMES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class DataThemeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("1"),
                dataThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        dataThemeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_datathemes_returns_valid_response() {
        DataThemes dataThemes =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/data-themes", DataThemes.class);

        assertEquals(DATA_THEMES_SIZE, dataThemes.getDataThemes().size());

        DataTheme first = dataThemes.getDataThemes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", first.getUri());
        assertEquals("AGRI", first.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("http://publications.europa.eu/resource/authority/data-theme", first.getConceptSchema().getUri());
        assertEquals("Data theme", first.getConceptSchema().getLabel().get(Language.ENGLISH.code()));
        assertEquals("20220715-0", first.getConceptSchema().getVersionNumber());
    }

    @Test
    public void test_if_get_datatheme_by_code_returns_valid_response() {
        DataTheme dataTheme =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/data-themes/AGRI", DataTheme.class);

        assertNotNull(dataTheme);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", dataTheme.getUri());
        assertEquals("AGRI", dataTheme.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", dataTheme.getLabel().get(Language.ENGLISH.code()));
        assertEquals("http://publications.europa.eu/resource/authority/data-theme", dataTheme.getConceptSchema().getUri());
        assertEquals("Data theme", dataTheme.getConceptSchema().getLabel().get(Language.ENGLISH.code()));
        assertEquals("20220715-0", dataTheme.getConceptSchema().getVersionNumber());
    }

    @Test
    public void test_if_post_data_themes_fails_without_api_key() {
        assertEquals(DATA_THEMES_SIZE, dataThemeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DATA_THEME.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/data-themes",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(DATA_THEMES_SIZE, dataThemeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DATA_THEME.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_data_themes_executes_a_force_update() {
        assertEquals(DATA_THEMES_SIZE, dataThemeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.DATA_THEME.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/data-themes",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(DATA_THEMES_SIZE, dataThemeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.DATA_THEME.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_data_theme_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/data-themes", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(EuroVocControllerIntegrationTest.class.getClassLoader().getResource("data-theme-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
