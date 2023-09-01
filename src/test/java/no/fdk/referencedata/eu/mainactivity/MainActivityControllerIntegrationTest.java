package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.mainactivity.LocalMainActivityHarvester.MAIN_ACTIVITIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class MainActivityControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MainActivityRepository mainActivityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("1"),
                mainActivityRepository,
                harvestSettingsRepository);

        mainActivityService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_access_rights_returns_valid_response() {
        MainActivities mainActivities =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/main-activities", MainActivities.class);

        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivities.getMainActivities().size());

        MainActivity first = mainActivities.getMainActivities().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/airport", first.getUri());
        assertEquals("airport", first.getCode());
        assertEquals("Airport-related activities", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_access_right_by_code_returns_valid_response() {
        MainActivity mainActivity =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/main-activities/health", MainActivity.class);

        assertNotNull(mainActivity);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/health", mainActivity.getUri());
        assertEquals("health", mainActivity.getCode());
        assertEquals("Health", mainActivity.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_access_rights_fails_without_api_key() {
        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivityRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/main-activities",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivityRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_access_rights_executes_a_force_update() {
        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivityRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/main-activities",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MAIN_ACTIVITIES_SIZE, mainActivityRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MAIN_ACTIVITY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
