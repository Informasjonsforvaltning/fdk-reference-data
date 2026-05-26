package no.fdk.referencedata.eu.continent;

import no.fdk.referencedata.eu.continent.ContinentWriter;
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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.continent.LocalContinentHarvester.CONTINENTS_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ContinentControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ContinentRepository continentRepository;

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

        ContinentService continentService = new ContinentService(
                new LocalContinentHarvester("1"),
                continentRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new ContinentWriter(continentRepository, rdfSourceRepository, harvestSettingsRepository));

        continentService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_continents_returns_valid_response() {
        Continents continents =
                restClient.get().uri("/eu/continents").retrieve().body(Continents.class);

        assertEquals(CONTINENTS_SIZE, continents.getContinents().size());

        Continent first = continents.getContinents().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/continent/AFRICA", first.getUri());
        assertEquals("AFRICA", first.getCode());
        assertEquals("Africa", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_continent_by_code_returns_valid_response() {
        Continent continent =
                restClient.get().uri("/eu/continents/EUROPE").retrieve().body(Continent.class);

        assertNotNull(continent);
        assertEquals("http://publications.europa.eu/resource/authority/continent/EUROPE", continent.getUri());
        assertEquals("EUROPE", continent.getCode());
        assertEquals("Europe", continent.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_continents_fails_without_api_key() {
        assertEquals(CONTINENTS_SIZE, continentRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CONTINENT.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/continents")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(CONTINENTS_SIZE, continentRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CONTINENT.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_continents_executes_a_force_update() {
        assertEquals(CONTINENTS_SIZE, continentRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CONTINENT.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/continents")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CONTINENTS_SIZE, continentRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CONTINENT.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_continents_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/continents", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ContinentControllerIntegrationTest.class.getClassLoader().getResource("continent-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
