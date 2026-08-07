package no.fdk.referencedata.graphql;

import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.country.Country;
import no.fdk.referencedata.eu.country.CountryRepository;
import no.fdk.referencedata.eu.country.CountryService;
import no.fdk.referencedata.eu.country.LocalCountryHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class CountryQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private CountryRepository countryRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        CountryService countryService = new CountryService(
                new LocalCountryHarvester(),
                countryRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        countryService.harvestAndSave();
    }

    @Test
    void test_if_countries_query_returns_all_countries() {
        GraphQlTester.EntityList<Country> result = graphQlTester.documentName("countries")
                .execute()
                .path("$['data']['countries']")
                .entityList(Country.class);

        result.hasSize(3);

        Country country = result.get().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/country/DEU", country.getUri());
        assertEquals("DEU", country.getCode());
        assertEquals("Germany", country.getLabel().get("en"));
    }

    @Test
    void test_if_country_by_code_query_returns_correct_country() {
        Country result = graphQlTester.documentName("country-by-code")
                .variable("code", "NOR")
                .execute()
                .path("$['data']['countryByCode']")
                .entity(Country.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/country/NOR", result.getUri());
        assertEquals("NOR", result.getCode());
        assertEquals("Norway", result.getLabel().get("en"));
    }

    @Test
    void test_if_country_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("country-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['countryByCode']")
                .valueIsNull();
    }
}
