package no.fdk.referencedata.apistatus;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
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
public class ApiStatusControllerIntegrationTest extends AbstractContainerTest {

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
    public void test_if_get_all_api_statuses_returns_valid_response() {
        ApiStatuses statuses =
                restClient.get().uri("/api-status").retrieve().body(ApiStatuses.class);

        assertEquals(4, statuses.getApiStatuses().size());

        ApiStatus last = statuses.getApiStatuses().get(3);
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/under-deprecation", last.getUri());
        assertEquals("DEPRECATED", last.getCode());
        assertEquals("Deprecated", last.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_api_status_by_code_returns_valid_response() {
        ApiStatus apiStatus =
                restClient.get().uri("/api-status/STABLE").retrieve().body(ApiStatus.class);

        assertNotNull(apiStatus);
        assertEquals("http://fellesdatakatalog.brreg.no/reference-data/codes/apistastus/production", apiStatus.getUri());
        assertEquals("STABLE", apiStatus.getCode());
        assertEquals("In production", apiStatus.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_api_status_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/api-status", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ApiStatusControllerIntegrationTest.class.getClassLoader().getResource("api-status-skos.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
