package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.mobility.conditions.LocalMobilityConditionHarvester;
import no.fdk.referencedata.mobility.conditions.MobilityCondition;
import no.fdk.referencedata.mobility.conditions.MobilityConditionRepository;
import no.fdk.referencedata.mobility.conditions.MobilityConditionService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class MobilityConditionQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityConditionRepository mobilityConditionRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        MobilityConditionService mobilityConditionService = new MobilityConditionService(
                new LocalMobilityConditionHarvester("1.0.0"),
                mobilityConditionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityConditionService.harvestAndSave(false);
    }

    @Test
    void test_if_mobility_conditions_query_returns_all_mobility_conditions() {
        List<MobilityCondition> result = graphQlTester.documentName("mobility-conditions")
                .execute()
                .path("$['data']['mobilityConditions']")
                .entityList(MobilityCondition.class)
                .get();

        assertEquals(10, result.size());

        MobilityCondition condition = result.get(0);

        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/contractual-arrangement", condition.getUri());
        assertEquals("contractual-arrangement", condition.getCode());
        assertEquals("Contractual arrangement", condition.getLabel().get("en"));
    }

    @Test
    void test_if_mobility_condition_by_code_public_query_returns_correct_condition() {
        MobilityCondition result = graphQlTester.documentName("mobility-condition-by-code")
                .variable("code", "licence-provided-free-of-charge")
                .execute()
                .path("$['data']['mobilityConditionByCode']")
                .entity(MobilityCondition.class)
                .get();

        assertEquals("https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage/licence-provided-free-of-charge", result.getUri());
        assertEquals("licence-provided-free-of-charge", result.getCode());
        assertEquals("Licence provided, free of charge", result.getLabel().get("en"));
    }

    @Test
    void test_if_mobility_theme_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("mobility-condition-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['mobilityConditionByCode']")
                .valueIsNull();
    }

}
