package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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
public class EuroVocControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EuroVocRepository euroVocRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        EuroVocService EuroVocService = new EuroVocService(
                new LocalEuroVocHarvester("1"),
                euroVocRepository,
                harvestSettingsRepository);

        EuroVocService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_eurovocs_returns_valid_response() {
        EuroVocs euroVocs =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/eurovocs", EuroVocs.class);

        assertEquals(7384, euroVocs.getEuroVocs().size());

        EuroVoc first = euroVocs.getEuroVocs().get(0);
        assertEquals("http://eurovoc.europa.eu/1", first.getUri());
        assertEquals("1", first.getCode());
        assertEquals("Århus (county)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_eurovoc_by_code_returns_valid_response() {
        EuroVoc euroVoc =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/eurovocs/337", EuroVoc.class);

        assertNotNull(euroVoc);
        assertEquals("http://eurovoc.europa.eu/337", euroVoc.getUri());
        assertEquals("337", euroVoc.getCode());
        assertEquals("regions of Denmark", euroVoc.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_eurovocs_fails_without_api_key() {
        assertEquals(7384, euroVocRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.EURO_VOC.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/eurovocs",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(7384, euroVocRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.EURO_VOC.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_data_themes_executes_a_force_update() {
        assertEquals(7384, euroVocRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.EURO_VOC.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/eurovocs",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(7384, euroVocRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.EURO_VOC.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
