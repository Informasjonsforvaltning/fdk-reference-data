package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeRepository;
import no.fdk.referencedata.digdir.audiencetype.AudienceTypeService;
import no.fdk.referencedata.digdir.audiencetype.LocalAudienceTypeHarvester;
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

import static org.junit.jupiter.api.Assertions.*;
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
class AudienceTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                new LocalAudienceTypeHarvester("1"),
                audienceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        audienceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_audience_types_query_returns_all_audience_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/audience-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", response.get("$['data']['audienceTypes'][0]['uri']"));
        assertEquals("public", response.get("$['data']['audienceTypes'][0]['code']"));
        assertEquals("public", response.get("$['data']['audienceTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_audience_type_by_code_public_query_returns_public_audience_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/audience-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "specialist")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/audience-type#specialist", response.get("$['data']['audienceTypeByCode']['uri']"));
        assertEquals("specialist", response.get("$['data']['audienceTypeByCode']['code']"));
        assertEquals("specialist", response.get("$['data']['audienceTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_audience_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/audience-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['audienceTypeByCode']"));
    }

}
