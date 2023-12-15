package no.fdk.referencedata.referencetypes;

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
public class ReferenceTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_reference_types_returns_valid_response() {
        ReferenceTypes referenceTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/reference-types", ReferenceTypes.class);

        assertEquals(11, referenceTypes.getReferenceTypes().size());

        ReferenceType first = referenceTypes.getReferenceTypes().get(0);
        assertEquals("http://purl.org/dc/terms/hasVersion", first.getUri());
        assertEquals("hasVersion", first.getCode());
        assertEquals("Has version", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_reference_type_by_code_returns_valid_response() {
        ReferenceType referenceType =
                this.restTemplate.getForObject("http://localhost:" + port + "/reference-types/isRequiredBy", ReferenceType.class);

        assertNotNull(referenceType);
        assertEquals("http://purl.org/dc/terms/isRequiredBy", referenceType.getUri());
        assertEquals("isRequiredBy", referenceType.getCode());
        assertEquals("Is required by", referenceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_reference_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/reference-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ReferenceTypeControllerIntegrationTest.class.getClassLoader().getResource("reference-code-skos.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
