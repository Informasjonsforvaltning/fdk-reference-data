package no.fdk.referencedata.eu.frequency;

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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.frequency.LocalFrequencyHarvester.FREQUENCIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class FrequencyControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private FrequencyRepository frequencyRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        FrequencyService frequencyService = new FrequencyService(
                new LocalFrequencyHarvester("1"),
                frequencyRepository,
                harvestSettingsRepository);

        frequencyService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_frequencies_returns_valid_response() {
        Frequencies frequencies =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/frequencies", Frequencies.class);

        assertEquals(FREQUENCIES_SIZE, frequencies.getFrequencies().size());

        Frequency first = frequencies.getFrequencies().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", first.getUri());
        assertEquals("ANNUAL", first.getCode());
        assertEquals("annual", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_frequency_by_code_returns_valid_response() {
        Frequency frequency =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/frequencies/ANNUAL", Frequency.class);

        assertNotNull(frequency);
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", frequency.getUri());
        assertEquals("ANNUAL", frequency.getCode());
        assertEquals("annual", frequency.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_frequencies_fails_without_api_key() {
        assertEquals(FREQUENCIES_SIZE, frequencyRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.FREQUENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/frequencies",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(FREQUENCIES_SIZE, frequencyRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.FREQUENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_frequencies_executes_a_force_update() {
        assertEquals(FREQUENCIES_SIZE, frequencyRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.FREQUENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/frequencies",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(FREQUENCIES_SIZE, frequencyRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.FREQUENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
