package no.fdk.referencedata.digdir.legalresourcetype;

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
public class LegalResourceTypeControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "legal-resource-type");
    }

    @Test
    public void test_if_get_all_legal_resource_types_returns_valid_response() {
        LegalResourceTypes legalResourceTypes =
                restClient.get().uri("/digdir/legal-resource-types").retrieve().body(LegalResourceTypes.class);
        LegalResourceType first = legalResourceTypes.getLegalResourceTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#act", first.getUri());
        assertEquals("act", first.getCode());
        assertEquals("act", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_legal_resource_type_by_code_returns_valid_response() {
        LegalResourceType legalResourceType =
                restClient.get().uri("/digdir/legal-resource-types/regulation").retrieve().body(LegalResourceType.class);

        assertNotNull(legalResourceType);
        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#regulation", legalResourceType.getUri());
        assertEquals("regulation", legalResourceType.getCode());
        assertEquals("regulation", legalResourceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_legal_resource_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/legal-resource-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(LegalResourceTypeControllerIntegrationTest.class.getClassLoader().getResource("legal-resource-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
