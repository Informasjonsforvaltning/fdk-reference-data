package no.fdk.referencedata.digdir.serviceoutputtype;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
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
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class ServiceOutputTypeControllerIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        HarvestTestSupport.harvest(registry, "service-output-type");
    }

    @Test
    public void test_if_get_all_service_output_types_returns_valid_response() {
        ServiceOutputTypes serviceOutputTypes =
                restClient.get().uri("/digdir/service-output-types").retrieve().body(ServiceOutputTypes.class);
        ServiceOutputType first = serviceOutputTypes.getServiceOutputTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/service-output-type#declaration", first.getUri());
        assertEquals("declaration", first.getCode());
        assertEquals("declaration", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_service_output_type_by_code_returns_valid_response() {
        ServiceOutputType serviceOutputType =
                restClient.get().uri("/digdir/service-output-types/permit").retrieve().body(ServiceOutputType.class);

        assertNotNull(serviceOutputType);
        assertEquals("https://data.norge.no/vocabulary/service-output-type#permit", serviceOutputType.getUri());
        assertEquals("permit", serviceOutputType.getCode());
        assertEquals("permit", serviceOutputType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_service_output_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/service-output-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ServiceOutputTypeControllerIntegrationTest.class.getClassLoader().getResource("service-output-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
