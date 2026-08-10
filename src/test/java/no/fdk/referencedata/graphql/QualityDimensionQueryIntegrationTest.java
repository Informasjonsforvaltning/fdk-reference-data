package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.qualitydimension.QualityDimension;
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
class QualityDimensionQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "quality-dimension");
    }

    @Test
    void test_if_quality_dimensions_query_returns_all_quality_dimensions() {
        List<QualityDimension> result = graphQlTester.documentName("quality-dimensions")
                .execute()
                .path("$['data']['qualityDimensions']")
                .entityList(QualityDimension.class)
                .get();
        QualityDimension qualityDimension = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/quality-dimension#accuracy", qualityDimension.getUri());
        assertEquals("accuracy", qualityDimension.getCode());
        assertEquals("nøyaktighet", qualityDimension.getLabel().get("nb"));
        assertEquals("accuracy", qualityDimension.getLabel().get("en"));
    }

    @Test
    void test_if_quality_dimension_by_code_query_returns_quality_dimension() {
        QualityDimension result = graphQlTester.documentName("quality-dimension-by-code")
                .variable("code", "completeness")
                .execute()
                .path("$['data']['qualityDimensionByCode']")
                .entity(QualityDimension.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/quality-dimension#completeness", result.getUri());
        assertEquals("completeness", result.getCode());
        assertEquals("fullstendighet", result.getLabel().get("nb"));
        assertEquals("completeness", result.getLabel().get("en"));
    }

}
