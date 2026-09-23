package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.productstatus.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class ProductStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "product-status");
    }

    @Test
    void test_if_product_statuses_query_returns_valid_response() {
        List<ProductStatus> result = graphQlTester.documentName("product-statuses")
                .execute()
                .path("$['data']['productStatuses']")
                .entityList(ProductStatus.class)
                .get();
        ProductStatus productStatus = result.get(0);

        assertEquals(
                "http://publications.europa.eu/resource/authority/product-status/DEVELOPMENT",
                productStatus.getUri()
        );
        assertEquals("DEVELOPMENT", productStatus.getCode());
        assertEquals("under utvikling", productStatus.getLabel().get("no"));
        assertEquals("under utvikling", productStatus.getLabel().get("nb"));
        assertEquals("under utvikling", productStatus.getLabel().get("nn"));
        assertEquals("in development", productStatus.getLabel().get("en"));
    }

    @Test
    void test_if_product_status_by_code_query_returns_valid_response() {
        ProductStatus result = graphQlTester.documentName("product-status-by-code")
                .variable("code", "PRODUCTION")
                .execute()
                .path("$['data']['productStatusByCode']")
                .entity(ProductStatus.class)
                .get();

        assertEquals(
                "http://publications.europa.eu/resource/authority/product-status/PRODUCTION",
                result.getUri()
        );
        assertEquals("PRODUCTION", result.getCode());
        assertEquals("i produksjon", result.getLabel().get("no"));
        assertEquals("i produksjon", result.getLabel().get("nb"));
        assertEquals("i produksjon", result.getLabel().get("nn"));
        assertEquals("in production", result.getLabel().get("en"));
    }

}
