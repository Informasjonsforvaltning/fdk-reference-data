package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.accessright.AccessRight;
import no.fdk.referencedata.eu.accessright.AccessRights;
import no.fdk.referencedata.i18n.Language;
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
public class KommuneControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private KommuneRepository kommuneRepository;

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
        KommuneService kommuneService = new KommuneService(
                new LocalKommuneHarvester(wiremockHost, wiremockPort),
                kommuneRepository,
                harvestSettingsRepository);

        kommuneService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_kommuner_returns_valid_response() {
        Kommuner kommuner =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/kommuner", Kommuner.class);

        assertEquals(4, kommuner.getKommuner().size());

        Kommune first = kommuner.getKommuner().get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/123456", first.getUri());
        assertEquals("Kommune 1", first.getKommunenavn());
        assertEquals("Kommune 1 norsk", first.getKommunenavnNorsk());
        assertEquals("123456", first.getKommunenummer());
    }

    @Test
    public void test_if_get_kommune_by_kommunenr_returns_valid_response() {
        Kommune kommune =
                this.restTemplate.getForObject("http://localhost:" + port + "/geonorge/administrative-enheter/kommuner/223456", Kommune.class);

        assertNotNull(kommune);
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/223456", kommune.getUri());
        assertEquals("Kommune 2", kommune.getKommunenavn());
        assertEquals("Kommune 2 norsk", kommune.getKommunenavnNorsk());
        assertEquals("223456", kommune.getKommunenummer());
    }

    @Test
    public void test_if_post_kommuner_fails_without_api_key() {
        assertEquals(4, kommuneRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name()).orElseThrow();
        assertEquals("0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/geonorge/administrative-enheter/kommuner",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(4, kommuneRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name()).orElseThrow();
        assertEquals("0", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_kommuner_executes_an_update() {
        assertEquals(4, kommuneRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name()).orElseThrow();
        assertEquals("0", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/geonorge/administrative-enheter/kommuner",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, kommuneRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.GEONORGE_KOMMUNE.name()).orElseThrow();
        assertEquals("0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
