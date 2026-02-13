package no.fdk.referencedata.mobility.theme;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

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
public class MobilityThemeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MobilityThemeRepository mobilityThemeRepository;

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

        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.0.0"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityThemeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_mobility_themes_returns_valid_response() {
        MobilityThemes mobilityThemes =
                restClient.get()
                        .uri("/mobility/themes")
                        .retrieve()
                        .body(MobilityThemes.class);

        assertEquals(123, mobilityThemes.getMobilityThemes().size());

        MobilityTheme first = mobilityThemes.getMobilityThemes().get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/accesibility-information-for-vehicles", first.getUri());
        assertEquals("accesibility-information-for-vehicles", first.getCode());
        assertEquals("Accesibility information for vehicles", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_mobility_theme_by_code_returns_valid_response() {
        MobilityTheme theme =
                restClient.get()
                        .uri("/mobility/themes/speed-limits")
                        .retrieve()
                        .body(MobilityTheme.class);

        assertNotNull(theme);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/speed-limits", theme.getUri());
        assertEquals("speed-limits", theme.getCode());
        assertEquals("Speed limits", theme.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_mobility_themes_fails_without_api_key() {
        assertEquals(123, mobilityThemeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post()
                .uri("/mobility/themes")
                .headers(h -> h.addAll(headers))
                .exchange((request, response2) -> ResponseEntity.status(response2.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(123, mobilityThemeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_mobility_themes_executes_a_force_update() {
        assertEquals(123, mobilityThemeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name()).orElseThrow();
        assertEquals("1.0.0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post()
                .uri("/mobility/themes")
                .headers(h -> h.addAll(headers))
                .exchange((request, response2) -> ResponseEntity.status(response2.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(123, mobilityThemeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MOBILITY_THEME.name()).orElseThrow();
        assertEquals("0.1.0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_mobility_themes_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/mobility/themes", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MobilityThemeControllerIntegrationTest.class.getClassLoader().getResource("mobility-themes.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
