package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategory;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryRepository;
import no.fdk.referencedata.eu.highvaluecategories.HighValueCategoryService;
import no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
    private HighValueCategoryRepository highValueCategoryRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("2.1"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        highValueCategoryService.harvestAndSave(false);
    }

    @Test
    void test_if_high_value_categories_query_returns_all_categories() {
        GraphQlTester.EntityList<HighValueCategory> result = graphQlTester.documentName("high-value-categories")
                .execute()
                .path("$['data']['highValueCategories']")
                .entityList(HighValueCategory.class);

        result.hasSize(LocalHighValueCategoryHarvester.HIGH_VALUE_CATEGORIES_SIZE);

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

    @Test
    void test_if_high_value_category_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("high-value-category-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['highValueCategoryByCode']")
                .valueIsNull();
    }

}
