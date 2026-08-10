package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

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

    @Autowired
    private ReferenceDataRegistry registry;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        HarvestTestSupport.harvest(registry, "country");
    }

    @Test
    public void test_if_get_all_countries_returns_valid_response() {
        Countries countries =
                restClient.get().uri("/eu/countries").retrieve().body(Countries.class);
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
    public void test_countries_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/countries", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(CountryControllerIntegrationTest.class.getClassLoader().getResource("country-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
