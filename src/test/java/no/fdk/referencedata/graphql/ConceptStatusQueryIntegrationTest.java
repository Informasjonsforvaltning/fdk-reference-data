package no.fdk.referencedata.graphql;

import no.fdk.referencedata.container.AbstractContainerTest;;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatus;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusService;
import no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
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
@ActiveProfiles("test")
class ConceptStatusQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private ConceptStatusRepository conceptStatusRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        ConceptStatusService conceptStatusService = new ConceptStatusService(
                new LocalConceptStatusHarvester("1"),
                conceptStatusRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        conceptStatusService.harvestAndSave(false);
    }

    @Test
    void test_if_concept_statuses_query_returns_valid_response() {
        List<ConceptStatus> result = graphQlTester.documentName("concept-statuses")
                .execute()
                .path("$['data']['conceptStatuses']")
                .entityList(ConceptStatus.class)
                .get();

        assertEquals(12, result.size());

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

    @Test
    void test_if_concept_status_by_code_query_returns_null() {
        graphQlTester.documentName("concept-status-by-code")
                .variable("code", "INVALID")
                .execute()
                .path("$['data']['conceptStatusByCode']")
                .valueIsNull();
    }
}
