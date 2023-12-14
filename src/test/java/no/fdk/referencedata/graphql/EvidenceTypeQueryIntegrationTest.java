package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeRepository;
import no.fdk.referencedata.digdir.evidencetype.EvidenceTypeService;
import no.fdk.referencedata.digdir.evidencetype.LocalEvidenceTypeHarvester;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class EvidenceTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private EvidenceTypeRepository evidenceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        EvidenceTypeService evidenceTypeService = new EvidenceTypeService(
                new LocalEvidenceTypeHarvester("1"),
                evidenceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        evidenceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_evidence_types_query_returns_all_evidence_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/evidence-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/evidence-type#attestation", response.get("$['data']['evidenceTypes'][0]['uri']"));
        assertEquals("attestation", response.get("$['data']['evidenceTypes'][0]['code']"));
        assertEquals("attestation", response.get("$['data']['evidenceTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_evidence_type_by_code_public_query_returns_public_evidence_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/evidence-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "protocol")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/evidence-type#protocol", response.get("$['data']['evidenceTypeByCode']['uri']"));
        assertEquals("protocol", response.get("$['data']['evidenceTypeByCode']['code']"));
        assertEquals("protocol", response.get("$['data']['evidenceTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_evidence_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/evidence-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['evidenceTypeByCode']"));
    }

}
