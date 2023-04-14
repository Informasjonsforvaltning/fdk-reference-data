package no.fdk.referencedata.provenancestatement;

import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
public class ProvenanceControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_api_specifications_returns_valid_response() {
        ProvenanceStatements provenanceStatements =
                this.restTemplate.getForObject("http://localhost:" + port + "/provenance-statements", ProvenanceStatements.class);

        assertEquals(4, provenanceStatements.getProvenanceStatements().size());

        ProvenanceStatement first = provenanceStatements.getProvenanceStatements().get(0);
        assertEquals("http://data.brreg.no/datakatalog/provinens/bruker", first.getUri());
        assertEquals("BRUKER", first.getCode());
        assertEquals("User collection", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_specification_by_code_returns_valid_response() {
        ProvenanceStatement nasjonal =
                this.restTemplate.getForObject("http://localhost:" + port + "/provenance-statements/NASJONAL", ProvenanceStatement.class);

        assertNotNull(nasjonal);
        assertEquals("http://data.brreg.no/datakatalog/provinens/nasjonal", nasjonal.getUri());
        assertEquals("NASJONAL", nasjonal.getCode());
        assertEquals("Authoritativ source", nasjonal.getLabel().get(Language.ENGLISH.code()));
    }
}
