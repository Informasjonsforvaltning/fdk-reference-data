package no.fdk.referencedata.eu.accessright;

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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.accessright.LocalAccessRightHarvester.ACCESS_RIGHTS_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class AccessRightControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AccessRightRepository accessRightRepository;

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

        AccessRightService accessRightService = new AccessRightService(
                new LocalAccessRightHarvester("1"),
                accessRightRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        accessRightService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_access_rights_returns_valid_response() {
        AccessRights accessRights =
                restClient.get().uri("/eu/access-rights").retrieve().body(AccessRights.class);

        assertEquals(ACCESS_RIGHTS_SIZE, accessRights.getAccessRights().size());

        AccessRight first = accessRights.getAccessRights().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", first.getUri());
        assertEquals("CONFIDENTIAL", first.getCode());
        assertEquals("confidential", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_access_right_by_code_returns_valid_response() {
        AccessRight accessRight =
                restClient.get().uri("/eu/access-rights/CONFIDENTIAL").retrieve().body(AccessRight.class);

        assertNotNull(accessRight);
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", accessRight.getUri());
        assertEquals("CONFIDENTIAL", accessRight.getCode());
        assertEquals("confidential", accessRight.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_access_rights_fails_without_api_key() {
        assertEquals(ACCESS_RIGHTS_SIZE, accessRightRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/access-rights")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(ACCESS_RIGHTS_SIZE, accessRightRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_access_rights_executes_a_force_update() {
        assertEquals(ACCESS_RIGHTS_SIZE, accessRightRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/access-rights")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ACCESS_RIGHTS_SIZE, accessRightRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.ACCESS_RIGHT.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_access_rights_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/access-rights", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(AccessRightControllerIntegrationTest.class.getClassLoader().getResource("access-right-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
