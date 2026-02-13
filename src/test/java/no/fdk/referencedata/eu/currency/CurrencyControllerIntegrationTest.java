package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static no.fdk.referencedata.eu.currency.LocalCurrencyHarvester.CURRENCY_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class CurrencyControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CurrencyRepository currencyRepository;

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

        CurrencyService currencyService = new CurrencyService(
                new LocalCurrencyHarvester("1"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        currencyService.harvestAndSave(true);
    }

    @Test
    public void test_if_get_all_currencies_returns_valid_response() {
        Currencies currencies =
                restClient.get().uri("/eu/currencies").retrieve().body(Currencies.class);

        assertEquals(CURRENCY_SIZE, currencies.getCurrencies().size());

        Currency first = currencies.getCurrencies().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/currency/AUD", first.getUri());
        assertEquals("AUD", first.getCode());
        assertEquals("Australian dollar", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_currency_by_code_returns_valid_response() {
        Currency currency =
                restClient.get().uri("/eu/currencies/ISK").retrieve().body(Currency.class);

        assertNotNull(currency);
        assertEquals("http://publications.europa.eu/resource/authority/currency/ISK", currency.getUri());
        assertEquals("ISK", currency.getCode());
        assertEquals("Iceland króna", currency.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Islandsk krone", currency.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertEquals("Islandsk krone", currency.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_post_currencies_fails_without_api_key() {
        assertEquals(CURRENCY_SIZE, currencyRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CURRENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/eu/currencies")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(CURRENCY_SIZE, currencyRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CURRENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsAfter.getLatestVersion());
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_currencies_executes_a_force_update() {
        assertEquals(CURRENCY_SIZE, currencyRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.CURRENCY.name()).orElseThrow();
        assertEquals("1", harvestSettingsBefore.getLatestVersion());
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/eu/currencies")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(CURRENCY_SIZE, currencyRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.CURRENCY.name()).orElseThrow();
        assertEquals("20241211-0", harvestSettingsAfter.getLatestVersion());
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_currencies_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/currencies", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(CurrencyControllerIntegrationTest.class.getClassLoader().getResource("currency-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
