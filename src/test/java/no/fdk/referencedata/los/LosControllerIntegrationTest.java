package no.fdk.referencedata.los;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class LosControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private LosRepository losRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        LosService losService = new LosService(
                new LocalLosImporter(),
                losRepository,
                rdfSourceRepository);

        losService.importLosNodes();
    }

    @Test
    public void test_if_get_all_los_nodes_returns_valid_response() {
        LosNodes losNodes =
                this.restTemplate.getForObject("http://localhost:" + port + "/los/themes-and-words", LosNodes.class);

        assertEquals(519, losNodes.getLosNodes().size());

        final LosNode first = losNodes.getLosNodes().get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertFalse(first.isTheme());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

    @Test
    public void test_if_get_los_nodes_by_uri_returns_valid_response() {
        LosNodes losNodes =
                this.restTemplate.getForObject("http://localhost:" + port + "/los/themes-and-words?uris={uris}",
                        LosNodes.class, Map.of("uris", "https://psi.norge.no/los/ord/abort"));

        assertEquals(1, losNodes.getLosNodes().size());
        final LosNode first = losNodes.getLosNodes().get(0);
        assertEquals("https://psi.norge.no/los/ord/abort", first.getUri());
        assertEquals("Abort", first.getName().get(Language.NORWEGIAN_BOKMAAL.code()));
        assertFalse(first.isTheme());
        assertEquals(List.of("helse-og-omsorg/svangerskap/abort"), first.getLosPaths());
        assertEquals(List.of(URI.create("https://psi.norge.no/los/tema/svangerskap")), first.getParents());
        assertEquals(List.of("Svangerskapsavbrudd", "Svangerskapsavbrot"), first.getSynonyms());
    }

    @Test
    public void test_los_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/los/themes-and-words", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LosControllerIntegrationTest.class.getClassLoader().getResource("los.rdf")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
