package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatus;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusRepository;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusService;
import no.fdk.referencedata.eu.distributionstatus.LocalDistributionStatusHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
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
import static org.mockito.Mockito.mock;

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
    private GraphQlTester graphQlTester;

    @Autowired
    private DistributionStatusRepository distributionStatusRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        DistributionStatusService distributionStatusService = new DistributionStatusService(
                new LocalDistributionStatusHarvester("1"),
                distributionStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        distributionStatusService.harvestAndSave(false);
    }

    @Test
    void test_if_distribution_statuses_query_returns_all_distribution_statuses() {
        List<DistributionStatus> result = graphQlTester.documentName("distribution-statuses")
                .execute()
                .path("$['data']['distributionStatuses']")
                .entityList(DistributionStatus.class)
                .get();

        Assertions.assertEquals(4, result.size());

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

    @Test
    void test_if_distribution_status_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("distribution-status-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['distributionStatusByCode']")
                .valueIsNull();
    }

}
