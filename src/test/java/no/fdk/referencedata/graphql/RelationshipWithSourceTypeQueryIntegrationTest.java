package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;

import no.fdk.referencedata.digdir.relationshipwithsourcetype.LocalRelationshipWithSourceTypeHarvester;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeRepository;
import no.fdk.referencedata.digdir.relationshipwithsourcetype.RelationshipWithSourceTypeService;
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
class RelationshipWithSourceTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RelationshipWithSourceTypeRepository relationshipWithSourceTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        RelationshipWithSourceTypeService relationshipWithSourceTypeService = new RelationshipWithSourceTypeService(
                new LocalRelationshipWithSourceTypeHarvester("1"),
                relationshipWithSourceTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        relationshipWithSourceTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_relationship_with_source_types_query_returns_all_relationship_with_source_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/relationship-with-source-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", response.get("$['data']['relationshipWithSourceTypes'][0]['uri']"));
        assertEquals("derived-from-source", response.get("$['data']['relationshipWithSourceTypes'][0]['code']"));
        assertEquals("derived from source", response.get("$['data']['relationshipWithSourceTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_relationship_with_source_type_by_code_derived_from_source_query_returns_derived_from_source_relationship_with_source_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/relationship-with-source-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "derived-from-source")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("https://data.norge.no/vocabulary/relationship-with-source-type#derived-from-source", response.get("$['data']['relationshipWithSourceTypeByCode']['uri']"));
        assertEquals("derived-from-source", response.get("$['data']['relationshipWithSourceTypeByCode']['code']"));
        assertEquals("derived from source", response.get("$['data']['relationshipWithSourceTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_relationship_with_source_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/relationship-with-source-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['relationshipWithSourceTypeByCode']"));
    }
}
