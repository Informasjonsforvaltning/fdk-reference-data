package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.serviceoutputtype.ServiceOutputType;
import org.junit.jupiter.api.Assertions;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class ServiceOutputTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "service-output-type");
    }

    @Test
    void test_if_service_output_types_query_returns_all_service_output_types() {
        List<ServiceOutputType> result = graphQlTester.documentName("service-output-types")
                .execute()
                .path("$['data']['serviceOutputTypes']")
                .entityList(ServiceOutputType.class)
                .get();
        ServiceOutputType serviceOutputType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/service-output-type#declaration", serviceOutputType.getUri());
        assertEquals("declaration", serviceOutputType.getCode());
        assertEquals("erklæring", serviceOutputType.getLabel().get("nb"));
        assertEquals("declaration", serviceOutputType.getLabel().get("en"));
    }

    @Test
    void test_if_service_output_type_by_code_query_returns_service_output_type() {
        ServiceOutputType result = graphQlTester.documentName("service-output-type-by-code")
                .variable("code", "permit")
                .execute()
                .path("$['data']['serviceOutputTypeByCode']")
                .entity(ServiceOutputType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/service-output-type#permit", result.getUri());
        assertEquals("permit", result.getCode());
        assertEquals("tillatelse", result.getLabel().get("nb"));
        assertEquals("permit", result.getLabel().get("en"));
    }

}
