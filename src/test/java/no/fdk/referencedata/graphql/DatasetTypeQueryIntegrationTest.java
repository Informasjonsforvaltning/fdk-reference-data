package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.datasettype.DatasetType;
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
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class DatasetTypeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

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
    void test_if_dataset_types_query_returns_all_dataset_types() {
        List<DatasetType> result = graphQlTester.documentName("dataset-types")
                .execute()
                .path("$['data']['datasetTypes']")
                .entityList(DatasetType.class)
                .get();

        assertEquals(24, result.size());

        DatasetType datasetType = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/APROF", datasetType.getUri());
        assertEquals("APROF", datasetType.getCode());
        assertEquals("Applikasjonsprofil", datasetType.getLabel().get("no"));
        assertEquals("Applikasjonsprofil", datasetType.getLabel().get("nb"));
        assertEquals("Applikasjonsprofil", datasetType.getLabel().get("nn"));
        assertEquals("Application profile", datasetType.getLabel().get("en"));
    }

    @Test
    void test_if_dataset_type_by_code_aac_query_returns_econ_dataset_type() {
        DatasetType result = graphQlTester.documentName("dataset-type-by-code")
                .variable("code", "NAL")
                .execute()
                .path("$['data']['datasetTypeByCode']")
                .entity(DatasetType.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/NAL", result.getUri());
        assertEquals("NAL", result.getCode());
        assertEquals("Autoritetsliste for entitetsnavn", result.getLabel().get("no"));
        assertEquals("Autoritetsliste for entitetsnavn", result.getLabel().get("nb"));
        assertEquals("Autoritetsliste for entitetsnamn", result.getLabel().get("nn"));
        assertEquals("Name authority list", result.getLabel().get("en"));
    }

    @Test
    void test_if_dataset_type_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("dataset-type-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['datasetTypeByCode']")
                .valueIsNull();
    }

}
