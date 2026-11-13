package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.highvaluedatasettype.HighValueDatasetType;
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
class HighValueDatasetTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "high-value-dataset-type");
    }

    @Test
    void test_if_high_value_dataset_types_query_returns_all_high_value_dataset_types() {
        List<HighValueDatasetType> result = graphQlTester.documentName("high-value-dataset-types")
                .execute()
                .path("$['data']['highValueDatasetTypes']")
                .entityList(HighValueDatasetType.class)
                .get();
        HighValueDatasetType highValueDatasetType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#eu-hvd", highValueDatasetType.getUri());
        assertEquals("eu-hvd", highValueDatasetType.getCode());
        assertEquals("Datasett av høy verdi (EU)", highValueDatasetType.getLabel().get("nb"));
        assertEquals("High-value dataset (EU)", highValueDatasetType.getLabel().get("en"));
    }

    @Test
    void test_if_service_output_type_by_code_query_returns_service_output_type() {
        HighValueDatasetType result = graphQlTester.documentName("high-value-dataset-type-by-code")
                .variable("code", "national-hvd")
                .execute()
                .path("$['data']['highValueDatasetTypeByCode']")
                .entity(HighValueDatasetType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#national-hvd", result.getUri());
        assertEquals("national-hvd", result.getCode());
        assertEquals("Nasjonalt datasett av høy verdi", result.getLabel().get("nb"));
        assertEquals("National high-value dataset", result.getLabel().get("en"));
    }

}
