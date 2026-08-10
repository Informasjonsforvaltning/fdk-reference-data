package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategory;
import no.fdk.referencedata.container.AbstractContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class HighValueCategoryQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "high-value-category");
    }

    @Test
    void test_if_high_value_categories_query_returns_all_categories() {
        GraphQlTester.EntityList<HighValueCategory> result = graphQlTester.documentName("high-value-categories")
                .execute()
                .path("$['data']['highValueCategories']")
                .entityList(HighValueCategory.class);
        HighValueCategory category = result.get().get(0);
        assertEquals("http://data.europa.eu/bna/c_03ba8d92", category.getUri());
        assertEquals("c_03ba8d92", category.getCode());
        assertEquals("Regular lock and bridge operating times", category.getLabel().get("en"));
    }

    @Test
    void test_if_high_value_category_by_code_query_returns_category() {
        HighValueCategory result = graphQlTester.documentName("high-value-category-by-code")
                .variable("code", "c_a9135398")
                .execute()
                .path("$['data']['highValueCategoryByCode']")
                .entity(HighValueCategory.class)
                .get();

        assertEquals("http://data.europa.eu/bna/c_a9135398", result.getUri());
        assertEquals("c_a9135398", result.getCode());
        assertEquals("Companies and company ownership", result.getLabel().get("en"));
    }

}
