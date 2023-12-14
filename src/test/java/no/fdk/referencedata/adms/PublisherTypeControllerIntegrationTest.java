package no.fdk.referencedata.adms;

import no.fdk.referencedata.adms.publishertype.PublisherType;
import no.fdk.referencedata.adms.publishertype.PublisherTypes;
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
public class PublisherTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_publisher_types_returns_valid_response() {
        PublisherTypes publisherTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/publisher-types", PublisherTypes.class);

        assertEquals(11, publisherTypes.getPublisherTypes().size());

        PublisherType publisherType = publisherTypes.getPublisherTypes().get(1);
        assertEquals("http://purl.org/adms/publishertype/Company", publisherType.getUri());
        assertEquals("Company", publisherType.getCode());
        assertEquals("Company", publisherType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_by_code_returns_valid_response() {
        PublisherType publisherType =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/publisher-types/IndustryConsortium", PublisherType.class);

        assertNotNull(publisherType);
        assertEquals("http://purl.org/adms/publishertype/IndustryConsortium", publisherType.getUri());
        assertEquals("IndustryConsortium", publisherType.getCode());
        assertEquals("Industry consortium", publisherType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_publisher_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/adms/publisher-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(PublisherTypeControllerIntegrationTest.class.getClassLoader().getResource("rdf/adms-publisher-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
