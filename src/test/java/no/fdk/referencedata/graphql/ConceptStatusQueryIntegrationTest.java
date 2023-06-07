package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusRepository;
import no.fdk.referencedata.eu.conceptstatus.ConceptStatusService;
import no.fdk.referencedata.eu.conceptstatus.LocalConceptStatusHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@ActiveProfiles("test")
class ConceptStatusQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

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
    void test_if_concept_statuses_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/concept-statuses.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/CANDIDATE", response.get("$['data']['conceptStatuses'][0]['uri']"));
        assertEquals("CANDIDATE", response.get("$['data']['conceptStatuses'][0]['code']"));
        assertEquals("candidate", response.get("$['data']['conceptStatuses'][0]['label']['en']"));
    }

    @Test
    void test_if_concept_status_by_code_query_returns_valid_response() throws IOException {
        GraphQLResponse response = template.perform("graphql/concept-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "REVISED")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/concept-status/REVISED", response.get("$['data']['conceptStatusByCode']['uri']"));
        assertEquals("REVISED", response.get("$['data']['conceptStatusByCode']['code']"));
        assertEquals("revised", response.get("$['data']['conceptStatusByCode']['label']['en']"));
    }

    @Test
    void test_if_concept_status_by_code_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/concept-status-by-code.graphql",
                mapper.valueToTree(Map.of("code", "INVALID")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['conceptStatusByCode']"));
    }
}
