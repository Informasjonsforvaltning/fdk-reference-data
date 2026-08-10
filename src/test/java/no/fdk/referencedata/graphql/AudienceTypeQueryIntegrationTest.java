package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.audiencetype.AudienceType;
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
class AudienceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "audience-type");
    }

    @Test
    void test_if_audience_types_query_returns_all_audience_types() {
        List<AudienceType> result = graphQlTester.documentName("audience-types")
                .execute()
                .path("$['data']['audienceTypes']")
                .entityList(AudienceType.class)
                .get();
        AudienceType audienceType = result.get(0);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", audienceType.getUri());
        assertEquals("public", audienceType.getCode());
        assertEquals("allmennheten", audienceType.getLabel().get("nb"));
        assertEquals("allmenta", audienceType.getLabel().get("nn"));
        assertEquals("public", audienceType.getLabel().get("en"));
    }

    @Test
    void test_if_audience_type_by_code_public_query_returns_public_audience_type() {
        AudienceType result = graphQlTester.documentName("audience-type-by-code")
                .variable("code", "specialist")
                .execute()
                .path("$['data']['audienceTypeByCode']")
                .entity(AudienceType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/audience-type#specialist", result.getUri());
        assertEquals("specialist", result.getCode());
        assertEquals("spesialist", result.getLabel().get("nb"));
        assertEquals("spesialist", result.getLabel().get("nn"));
        assertEquals("specialist", result.getLabel().get("en"));
    }

}
