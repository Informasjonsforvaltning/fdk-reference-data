package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.eu.country.CountryWriter;
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

import static no.fdk.referencedata.eu.country.LocalCountryHarvester.COUNTRIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class CountryControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CountryRepository countryRepository;

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

        CountryService countryService = new CountryService(
                new LocalCountryHarvester("1"),
                countryRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new CountryWriter(countryRepository, rdfSourceRepository, harvestSettingsRepository));

        countryService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_countries_returns_valid_response() {
        Countries countries =
                restClient.get().uri("/eu/countries").retrieve().body(Countries.class);

        assertEquals(COUNTRIES_SIZE, countries.getCountries().size());

        Country first = countries.getCountries().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/country/DEU", first.getUri());
        assertEquals("DEU", first.getCode());
        assertEquals("Germany", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_country_by_code_returns_valid_response() {
        Country country =
                restClient.get().uri("/eu/countries/NOR").retrieve().body(Country.class);

        assertNotNull(country);
        assertEquals("http://publications.europa.eu/resource/authority/country/NOR", country.getUri());
        assertEquals("NOR", country.getCode());
        assertEquals("Norway", country.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_post_countries_fails_without_api_key() {
        assertEquals(COUNTRIES_SIZE, countryRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.COUNTRY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/countries")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(COUNTRIES_SIZE, countryRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.COUNTRY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_countries_executes_a_force_update() {
        assertEquals(COUNTRIES_SIZE, countryRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.COUNTRY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/countries")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(COUNTRIES_SIZE, countryRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.COUNTRY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_countries_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/countries", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(CountryControllerIntegrationTest.class.getClassLoader().getResource("country-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
