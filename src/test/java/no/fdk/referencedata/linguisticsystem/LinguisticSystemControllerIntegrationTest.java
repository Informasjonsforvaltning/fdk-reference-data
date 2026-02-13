package no.fdk.referencedata.linguisticsystem;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

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

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    public void test_if_get_all_linguistic_systems_returns_valid_response() {
        LinguisticSystems languages =
                restClient.get()
                        .uri("/linguistic-systems")
                        .retrieve()
                        .body(LinguisticSystems.class);

        assertEquals(5, languages.getLinguisticSystems().size());

        LinguisticSystem first = languages.getLinguisticSystems().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/language/ENG", first.getUri());
        assertEquals("ENG", first.getCode());
        assertEquals("English", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_linguistic_system_by_code_returns_valid_response() {
        LinguisticSystem language =
                restClient.get()
                        .uri("/linguistic-systems/NOB")
                        .retrieve()
                        .body(LinguisticSystem.class);

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
