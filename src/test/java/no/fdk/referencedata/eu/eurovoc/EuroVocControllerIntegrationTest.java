package no.fdk.referencedata.eu.eurovoc;

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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class EuroVocControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "eurovoc");
    }

    @Test
    public void test_if_get_all_eurovocs_returns_valid_response() {
        EuroVocs euroVocs =
                restClient.get().uri("/eu/eurovocs").retrieve().body(EuroVocs.class);
        EuroVoc first = euroVocs.getEuroVocs().get(0);
        assertEquals("http://eurovoc.europa.eu/1", first.getUri());
        assertEquals("1", first.getCode());
        assertEquals("Århus (county)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_eurovoc_by_code_returns_valid_response() {
        EuroVoc euroVoc =
                restClient.get().uri("/eu/eurovocs/337").retrieve().body(EuroVoc.class);

        assertNotNull(euroVoc);
        assertEquals("http://eurovoc.europa.eu/337", euroVoc.getUri());
        assertEquals("337", euroVoc.getCode());
        assertEquals("regions of Denmark", euroVoc.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_eurovoc_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/eurovocs", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(EuroVocControllerIntegrationTest.class.getClassLoader().getResource("eurovoc-with-fdk-triples.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
