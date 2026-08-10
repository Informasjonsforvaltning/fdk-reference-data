package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailability;
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
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class PlannedAvailabilityQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "planned-availability");
    }

    @Test
    void test_if_distribution_statuses_query_returns_all_planned_availabilities() {
        List<PlannedAvailability> result = graphQlTester.documentName("planned-availabilities")
                .execute()
                .path("$['data']['plannedAvailabilities']")
                .entityList(PlannedAvailability.class)
                .get();
        PlannedAvailability plannedAvailability = result.get(0);

        assertEquals(
                "http://publications.europa.eu/resource/authority/planned-availability/AVAILABLE",
                plannedAvailability.getUri()
        );
        assertEquals("AVAILABLE", plannedAvailability.getCode());
        assertEquals("tilgjengelig", plannedAvailability.getLabel().get("no"));
        assertEquals("tilgjengelig", plannedAvailability.getLabel().get("nb"));
        assertEquals("tilgjengeleg", plannedAvailability.getLabel().get("nn"));
        assertEquals("available", plannedAvailability.getLabel().get("en"));
    }

    @Test
    void test_if_distribution_status_by_code_returns_correct_distribution_status() {
        PlannedAvailability result = graphQlTester.documentName("planned-availability-by-code")
                .variable("code", "TEMPORARY")
                .execute()
                .path("$['data']['plannedAvailabilityByCode']")
                .entity(PlannedAvailability.class)
                .get();

        assertEquals(
                "http://publications.europa.eu/resource/authority/planned-availability/TEMPORARY",
                result.getUri()
        );
        assertEquals("TEMPORARY", result.getCode());
        assertEquals("midlertidig", result.getLabel().get("no"));
        assertEquals("midlertidig", result.getLabel().get("nb"));
        assertEquals("midlertidig", result.getLabel().get("nn"));
        assertEquals("temporary", result.getLabel().get("en"));
    }

}
