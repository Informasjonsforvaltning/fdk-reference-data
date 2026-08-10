package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.currency.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class CurrencyQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "currency");
    }

    @Test
    void test_if_currencies_query_returns_valid_response() {
        List<Currency> result = graphQlTester.documentName("currencies")
                .execute()
                .path("$['data']['currencies']")
                .entityList(Currency.class)
                .get();
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

}
