package no.fdk.referencedata.eu.continent;

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
public class ContinentControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "continent");
    }

    @Test
    public void test_if_get_all_continents_returns_valid_response() {
        Continents continents =
                restClient.get().uri("/eu/continents").retrieve().body(Continents.class);
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
    public void test_continents_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/continents", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ContinentControllerIntegrationTest.class.getClassLoader().getResource("continents-translated.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
