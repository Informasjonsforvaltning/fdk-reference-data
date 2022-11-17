package no.fdk.referencedata.digdir.servicechanneltype;

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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

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
public class ServiceChannelTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester("1"),
                serviceChannelTypeRepository,
                harvestSettingsRepository);

        serviceChannelTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_serviceChannel_types_returns_valid_response() {
        ServiceChannelTypes serviceChannelTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/service-channel-types", ServiceChannelTypes.class);

        assertEquals(11, serviceChannelTypes.getServiceChannelTypes().size());

        ServiceChannelType first = serviceChannelTypes.getServiceChannelTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#assistant", first.getUri());
        assertEquals("assistant", first.getCode());
        assertEquals("assistant", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_serviceChannel_type_by_code_returns_valid_response() {
        ServiceChannelType serviceChannelType =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/service-channel-types/telephone", ServiceChannelType.class);

        assertNotNull(serviceChannelType);
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#telephone", serviceChannelType.getUri());
        assertEquals("telephone", serviceChannelType.getCode());
        assertEquals("telephone", serviceChannelType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_serviceChannel_types_fails_without_api_key() {
        assertEquals(11, serviceChannelTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/service-channel-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(11, serviceChannelTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_serviceChannel_types_executes_a_force_update() {
        assertEquals(11, serviceChannelTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/service-channel-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(11, serviceChannelTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.SERVICE_CHANNEL_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }
}
