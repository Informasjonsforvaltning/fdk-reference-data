package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.legalresourcetype.LegalResourceType;
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
class LegalResourceTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "legal-resource-type");
    }

    @Test
    void test_if_legal_resource_types_query_returns_all_legal_resource_types() {
        List<LegalResourceType> result = graphQlTester.documentName("legal-resource-types")
                .execute()
                .path("$['data']['legalResourceTypes']")
                .entityList(LegalResourceType.class)
                .get();
        LegalResourceType legalResourceType = result.get(0);

        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#act", legalResourceType.getUri());
        assertEquals("act", legalResourceType.getCode());
        assertEquals("lov", legalResourceType.getLabel().get("nb"));
        assertEquals("act", legalResourceType.getLabel().get("en"));
    }

    @Test
    void test_if_legal_resource_type_by_code_query_returns_legal_resource_type() {
        LegalResourceType result = graphQlTester.documentName("legal-resource-type-by-code")
                .variable("code", "regulation")
                .execute()
                .path("$['data']['legalResourceTypeByCode']")
                .entity(LegalResourceType.class)
                .get();

        assertEquals("https://data.norge.no/vocabulary/legal-resource-type#regulation", result.getUri());
        assertEquals("regulation", result.getCode());
        assertEquals("forskrift", result.getLabel().get("nb"));
        assertEquals("regulation", result.getLabel().get("en"));
    }

}
