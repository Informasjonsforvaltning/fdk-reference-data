package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class FylkeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FylkeRepository fylkeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    @BeforeEach
    public void setup() {
        FylkeService fylkeService = new FylkeService(
                new LocalFylkeHarvester(wiremockHost, wiremockPort),
                fylkeRepository,
                harvestSettingsRepository);

        fylkeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_fylker_returns_valid_response() {
        Fylker fylker =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/fylker", Fylker.class);

        assertEquals(4, fylker.getFylker().size());

        Fylke first = fylker.getFylker().get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/123456", first.getUri());
        assertEquals("Fylke 1", first.getFylkesnavn());
        assertEquals("123456", first.getFylkesnummer());
    }

    @Test
    public void test_if_get_fylke_by_fylkenr_returns_valid_response() {
        Fylke fylke =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/fylker/223456", Fylke.class);

        assertNotNull(fylke);
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/223456", fylke.getUri());
        assertEquals("Fylke 2", fylke.getFylkesnavn());
        assertEquals("223456", fylke.getFylkesnummer());
    }

    @Test
    public void test_if_post_fylker_fails_without_api_key() {
        assertEquals(4, fylkeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name()).orElseThrow();
        assertEquals("0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/geonorge/administrative-enheter/fylker",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(4, fylkeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name()).orElseThrow();
        assertEquals("0", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_fylker_executes_an_update() {
        assertEquals(4, fylkeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name()).orElseThrow();
        assertEquals("0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/geonorge/administrative-enheter/fylker",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, fylkeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.GEONORGE_FYLKE.name()).orElseThrow();
        assertEquals("0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
