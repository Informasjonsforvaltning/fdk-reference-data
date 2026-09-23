package no.fdk.referencedata.eu.productstatus;

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
public class ProductStatusControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "product-status");
    }

    @Test
    public void test_if_get_all_product_statuses_returns_valid_response() {
        ProductStatuses productStatuses =
                restClient.get().uri("/eu/product-statuses").retrieve().body(ProductStatuses.class);
        ProductStatus first = productStatuses.getProductStatuses().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/product-status/DEVELOPMENT", first.getUri());
        assertEquals("DEVELOPMENT", first.getCode());
        assertEquals("in development", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_product_status_by_code_returns_valid_response() {
        ProductStatus productStatus =
                restClient.get().uri("/eu/product-statuses/PRODUCTION").retrieve().body(ProductStatus.class);

        assertNotNull(productStatus);
        assertEquals("http://publications.europa.eu/resource/authority/product-status/PRODUCTION", productStatus.getUri());
        assertEquals("PRODUCTION", productStatus.getCode());
        assertEquals("i produksjon", productStatus.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_product_statuses_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/product-statuses", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(ProductStatusControllerIntegrationTest.class.getClassLoader().getResource("product-status-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
