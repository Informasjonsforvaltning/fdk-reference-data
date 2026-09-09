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
                "http://publications.europa.eu/resource/authority/distribution-status/ARCHIVE",
                distributionStatus.getUri()
        );
        assertEquals("ARCHIVE", distributionStatus.getCode());
        assertEquals("arkivert", distributionStatus.getLabel().get("nb"));
        assertEquals("arkivert", distributionStatus.getLabel().get("nn"));
        assertEquals("historical archive", distributionStatus.getLabel().get("en"));
    }

    @Test
    void test_if_distribution_status_by_code_returns_translated_labels() {
        DistributionStatus result = graphQlTester.documentName("distribution-status-by-code")
                .variable("code", "ONGOING")
                .execute()
                .path("$['data']['distributionStatusByCode']")
                .entity(DistributionStatus.class)
                .get();

        assertEquals(
                "http://publications.europa.eu/resource/authority/distribution-status/ONGOING",
                result.getUri()
        );
        assertEquals("ONGOING", result.getCode());
        assertEquals("pågående", result.getLabel().get("nb"));
        assertEquals("pågåande", result.getLabel().get("nn"));
        assertEquals("ongoing", result.getLabel().get("en"));
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
