package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatus;
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
class DistributionStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "distribution-status");
    }

    @Test
    void test_if_distribution_statuses_query_returns_all_distribution_statuses() {
        List<DistributionStatus> result = graphQlTester.documentName("distribution-statuses")
                .execute()
                .path("$['data']['distributionStatuses']")
                .entityList(DistributionStatus.class)
                .get();
        DistributionStatus distributionStatus = result.get(0);

        assertEquals(
                "http://publications.europa.eu/resource/authority/distribution-status/COMPLETED",
                distributionStatus.getUri()
        );
        assertEquals("COMPLETED", distributionStatus.getCode());
        assertEquals("ferdigstilt", distributionStatus.getLabel().get("no"));
        assertEquals("ferdigstilt", distributionStatus.getLabel().get("nb"));
        assertEquals("ferdigstilt", distributionStatus.getLabel().get("nn"));
        assertEquals("completed", distributionStatus.getLabel().get("en"));
    }

    @Test
    void test_if_distribution_status_by_code_returns_correct_distribution_status() {
        DistributionStatus result = graphQlTester.documentName("distribution-status-by-code")
                .variable("code", "WITHDRAWN")
                .execute()
                .path("$['data']['distributionStatusByCode']")
                .entity(DistributionStatus.class)
                .get();

        assertEquals(
                "http://publications.europa.eu/resource/authority/distribution-status/WITHDRAWN",
                result.getUri()
        );
        assertEquals("WITHDRAWN", result.getCode());
        assertEquals("trukket tilbake", result.getLabel().get("no"));
        assertEquals("trukket tilbake", result.getLabel().get("nb"));
        assertEquals("trekt tilbake", result.getLabel().get("nn"));
        assertEquals("withdrawn", result.getLabel().get("en"));
    }

}
