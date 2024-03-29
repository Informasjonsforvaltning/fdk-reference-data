package no.fdk.referencedata.linguisticsystem;

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
public class LinguisticSystemControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_linguistic_systems_returns_valid_response() {
        LinguisticSystems languages =
                this.restTemplate.getForObject("http://localhost:" + port + "/linguistic-systems", LinguisticSystems.class);

        assertEquals(5, languages.getLinguisticSystems().size());

        LinguisticSystem first = languages.getLinguisticSystems().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_linguistic_system_by_code_returns_valid_response() {
        LinguisticSystem language =
                this.restTemplate.getForObject("http://localhost:" + port + "/linguistic-systems/NOB", LinguisticSystem.class);

        assertNotNull(language);
        assertEquals("http://publications.europa.eu/resource/authority/language/NOB", language.getUri());
        assertEquals("NOB", language.getCode());
        assertEquals("Norwegian Bokmål", language.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_linguistic_systems_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/linguistic-systems", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LinguisticSystemControllerIntegrationTest.class.getClassLoader().getResource("languages-skos.rdf")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
