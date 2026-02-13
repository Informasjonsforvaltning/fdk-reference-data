package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.currency.Currency;
import no.fdk.referencedata.eu.currency.CurrencyRepository;
import no.fdk.referencedata.eu.currency.CurrencyService;
import no.fdk.referencedata.eu.currency.LocalCurrencyHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static no.fdk.referencedata.eu.currency.LocalCurrencyHarvester.CURRENCY_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class CurrencyQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private CurrencyRepository currencyRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        CurrencyService currencyService = new CurrencyService(
                new LocalCurrencyHarvester("20241211-0"),
                currencyRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        currencyService.harvestAndSave(false);
    }

    @Test
    void test_if_currencies_query_returns_valid_response() {
        List<Currency> result = graphQlTester.documentName("currencies")
                .execute()
                .path("$['data']['currencies']")
                .entityList(Currency.class)
                .get();

        assertEquals(CURRENCY_SIZE, result.size());

        Currency conceptStatus = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/currency/AUD", conceptStatus.getUri());
        assertEquals("AUD", conceptStatus.getCode());
        assertEquals("Australian dollar", conceptStatus.getLabel().get("en"));
    }

    @Test
    void test_if_currency_by_code_query_returns_valid_response() {
        Currency result = graphQlTester.documentName("currency-by-code")
                .variable("code", "NOK")
                .execute()
                .path("$['data']['currencyByCode']")
                .entity(Currency.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/currency/NOK", result.getUri());
        assertEquals("NOK", result.getCode());
        assertEquals("Norsk krone", result.getLabel().get("nb"));
        assertEquals("Norsk krone", result.getLabel().get("nn"));
        assertEquals("Norwegian krone", result.getLabel().get("en"));
    }

    @Test
    void test_if_concept_status_by_code_query_returns_null() {
        graphQlTester.documentName("currency-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['currencyByCode']")
                .valueIsNull();
    }
}
