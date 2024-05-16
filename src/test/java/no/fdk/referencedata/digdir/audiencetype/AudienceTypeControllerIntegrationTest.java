package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypes;
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

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class AudienceTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("1"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        audienceTypeService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_audience_types_returns_valid_response() {
        AudienceTypes audienceTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/audience-types", AudienceTypes.class);

        assertEquals(2, audienceTypes.getAudienceTypes().size());

        AudienceType first = audienceTypes.getAudienceTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", first.getUri());
        assertEquals("public", first.getCode());
        assertEquals("public", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_audience_type_by_code_returns_valid_response() {
        AudienceType audienceType =
                this.restTemplate.getForObject("http://localhost:" + port + "/digdir/audience-types/public", AudienceType.class);

        assertNotNull(audienceType);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", audienceType.getUri());
        assertEquals("public", audienceType.getCode());
        assertEquals("public", audienceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_audience_types_fails_without_api_key() {
        assertEquals(2, audienceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.AUDIENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/audience-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(2, audienceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.AUDIENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_audience_types_executes_a_force_update() {
        assertEquals(2, audienceTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.AUDIENCE_TYPE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/digdir/audience-types",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, audienceTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.AUDIENCE_TYPE.name()).orElseThrow();
        assertEquals("2023-03-16", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_audience_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/audience-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(AudienceTypeControllerIntegrationTest.class.getClassLoader().getResource("audience-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
