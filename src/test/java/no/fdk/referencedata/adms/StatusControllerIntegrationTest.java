package no.fdk.referencedata.adms;

import no.fdk.referencedata.adms.status.ADMSStatus;
import no.fdk.referencedata.adms.status.ADMSStatuses;
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
public class StatusControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_if_get_all_statuses_returns_valid_response() {
        ADMSStatuses statuses =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/statuses", ADMSStatuses.class);

        assertEquals(4, statuses.getStatuses().size());

        ADMSStatus status = statuses.getStatuses().get(1);
        assertEquals("http://purl.org/adms/status/Deprecated", status.getUri());
        assertEquals("Deprecated", status.getCode());
        assertEquals("Deprecated", status.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_by_code_returns_valid_response() {
        ADMSStatus status =
                this.restTemplate.getForObject("http://localhost:" + port + "/adms/statuses/UnderDevelopment", ADMSStatus.class);

        assertNotNull(status);
        assertEquals("http://purl.org/adms/status/UnderDevelopment", status.getUri());
        assertEquals("UnderDevelopment", status.getCode());
        assertEquals("Under development", status.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_adms_status_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/adms/statuses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(PublisherTypeControllerIntegrationTest.class.getClassLoader().getResource("rdf/adms-status.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
