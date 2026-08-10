package no.fdk.referencedata.eu.currency;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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
import org.springframework.test.context.ActiveProfiles;

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
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        CurrencyService currencyService = new CurrencyService(
                LocalHarvesters.currency(),
                currencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        currencyService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_currencies_returns_valid_response() {
        Currencies currencies =
                restClient.get().uri("/eu/currencies").retrieve().body(Currencies.class);
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
    public void test_currencies_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/currencies", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(CurrencyControllerIntegrationTest.class.getClassLoader().getResource("currencies-translated.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
