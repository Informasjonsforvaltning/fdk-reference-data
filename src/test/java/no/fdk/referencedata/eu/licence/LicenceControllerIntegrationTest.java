package no.fdk.referencedata.eu.licence;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.licence.LocalLicenceHarvester.LICENCES_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class LicenceControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LicenceRepository licenceRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        LicenceService licenceService = new LicenceService(
                new LocalLicenceHarvester("1"),
                licenceRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        licenceService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_licences_returns_valid_response() {
        Licences licences =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/licences", Licences.class);

        assertEquals(LICENCES_SIZE, licences.getLicences().size());

        Licence first = licences.getLicences().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/licence/0BSD", first.getUri());
        assertEquals("0BSD", first.getCode());
        assertEquals("Zero-Clause BSD / Free Public License 1.0.0 (0BSD)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_licence_by_code_returns_valid_response() {
        Licence licence =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/licences/CC0", Licence.class);

        assertNotNull(licence);
        assertEquals("http://publications.europa.eu/resource/authority/licence/CC0", licence.getUri());
        assertEquals("CC0", licence.getCode());
        assertEquals("Creative Commons CC0 1.0 Universal", licence.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_licences_fails_without_api_key() {
        assertEquals(LICENCES_SIZE, licenceRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.LICENCE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/licences",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(LICENCES_SIZE, licenceRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.LICENCE.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_licences_executes_a_force_update() {
        assertEquals(LICENCES_SIZE, licenceRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.LICENCE.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = this.restTemplate.exchange("http://localhost:" + port + "/eu/licences",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LICENCES_SIZE, licenceRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.LICENCE.name()).orElseThrow();
        assertEquals("20241211-0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_licences_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/licences", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LicenceControllerIntegrationTest.class.getClassLoader().getResource("licences-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
} 
