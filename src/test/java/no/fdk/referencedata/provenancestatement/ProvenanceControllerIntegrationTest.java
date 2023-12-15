package no.fdk.referencedata.provenancestatement;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class ProvenanceControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_provenance_statements_returns_valid_response() {
        ProvenanceStatements provenanceStatements =
                this.restTemplate.getForObject("http://localhost:" + port + "/provenance-statements", ProvenanceStatements.class);

        assertEquals(4, provenanceStatements.getProvenanceStatements().size());

        ProvenanceStatement first = provenanceStatements.getProvenanceStatements().get(0);
        assertEquals("http://data.brreg.no/datakatalog/provinens/bruker", first.getUri());
        assertEquals("BRUKER", first.getCode());
        assertEquals("User collection", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_provenance_statement_by_code_returns_valid_response() {
        ProvenanceStatement nasjonal =
                this.restTemplate.getForObject("http://localhost:" + port + "/provenance-statements/NASJONAL", ProvenanceStatement.class);

        assertNotNull(nasjonal);
        assertEquals("http://data.brreg.no/datakatalog/provinens/nasjonal", nasjonal.getUri());
        assertEquals("NASJONAL", nasjonal.getCode());
        assertEquals("Authoritativ source", nasjonal.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_provenance_statements_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/provenance-statements", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ProvenanceControllerIntegrationTest.class.getClassLoader().getResource("provenance.rdf")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
