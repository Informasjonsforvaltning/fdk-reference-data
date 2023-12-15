package no.fdk.referencedata.apispecification;

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
public class ApiSpecificationControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_api_specifications_returns_valid_response() {
        ApiSpecifications apiSpecifications =
                this.restTemplate.getForObject("http://localhost:" + port + "/api-specifications", ApiSpecifications.class);

        assertEquals(2, apiSpecifications.getApiSpecifications().size());

        ApiSpecification first = apiSpecifications.getApiSpecifications().get(0);
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", first.getUri());
        assertEquals("account", first.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", first.getSource());
        assertEquals("Account details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_specification_by_code_returns_valid_response() {
        ApiSpecification apiSpecification =
                this.restTemplate.getForObject("http://localhost:" + port + "/api-specifications/account", ApiSpecification.class);

        assertNotNull(apiSpecification);
        assertEquals("https://data.norge.no/reference-data/api-specifications/account", apiSpecification.getUri());
        assertEquals("account", apiSpecification.getCode());
        assertEquals("https://bitsnorge.github.io/dsop-accounts-api", apiSpecification.getSource());
        assertEquals("Account details", apiSpecification.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_api_specifications_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/api-specifications", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ApiSpecificationControllerIntegrationTest.class.getClassLoader().getResource("rdf/api-specification-skos.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
