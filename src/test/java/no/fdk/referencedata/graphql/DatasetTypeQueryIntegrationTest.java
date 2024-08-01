package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.datasettype.DatasetTypeRepository;
import no.fdk.referencedata.eu.datasettype.DatasetTypeService;
import no.fdk.referencedata.eu.datasettype.LocalDatasetTypeHarvester;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class DatasetTypeQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        DatasetTypeService datasetTypeService = new DatasetTypeService(
                new LocalDatasetTypeHarvester("1"),
                datasetTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        datasetTypeService.harvestAndSave(false);
    }

    @Test
    void test_if_dataset_types_query_returns_all_dataset_types() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/dataset-types.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/APROF", response.get("$['data']['datasetTypes'][0]['uri']"));
        assertEquals("APROF", response.get("$['data']['datasetTypes'][0]['code']"));
        assertEquals("Application profile", response.get("$['data']['datasetTypes'][0]['label']['en']"));
    }

    @Test
    void test_if_dataset_type_by_code_aac_query_returns_econ_dataset_type() throws IOException {
        GraphQLResponse response = template.perform("graphql/dataset-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "NAL")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/NAL", response.get("$['data']['datasetTypeByCode']['uri']"));
        assertEquals("NAL", response.get("$['data']['datasetTypeByCode']['code']"));
        assertEquals("Name authority list", response.get("$['data']['datasetTypeByCode']['label']['en']"));
    }

    @Test
    void test_if_dataset_type_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/dataset-type-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['datasetTypeByCode']"));
    }

}
