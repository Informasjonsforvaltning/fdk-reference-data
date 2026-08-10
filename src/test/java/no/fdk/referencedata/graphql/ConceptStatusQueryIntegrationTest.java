package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
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
class ConceptStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "concept-status");
    }

    @Test
    void test_if_concept_statuses_query_returns_valid_response() {
        List<ConceptStatus> result = graphQlTester.documentName("concept-statuses")
                .execute()
                .path("$['data']['conceptStatuses']")
                .entityList(ConceptStatus.class)
                .get();
        ConceptStatus conceptStatus = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", conceptStatus.getUri());
        assertEquals("CANDIDATE", conceptStatus.getCode());
        assertEquals("kandidat", conceptStatus.getLabel().get("no"));
        assertEquals("kandidat", conceptStatus.getLabel().get("nb"));
        assertEquals("kandidat", conceptStatus.getLabel().get("nn"));
        assertEquals("candidate", conceptStatus.getLabel().get("en"));
    }

    @Test
    void test_if_concept_status_by_code_query_returns_valid_response() {
        ConceptStatus result = graphQlTester.documentName("concept-status-by-code")
                .variable("code", "REVISED")
                .execute()
                .path("$['data']['conceptStatusByCode']")
                .entity(ConceptStatus.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/concept-status/REVISED", result.getUri());
        assertEquals("REVISED", result.getCode());
        assertEquals("revidert", result.getLabel().get("no"));
        assertEquals("revidert", result.getLabel().get("nb"));
        assertEquals("revidert", result.getLabel().get("nn"));
        assertEquals("revised", result.getLabel().get("en"));
    }

}
