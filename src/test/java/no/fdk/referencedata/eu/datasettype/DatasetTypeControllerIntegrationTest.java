package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class DatasetTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        DatasetTypeService datasetTypeService = new DatasetTypeService(
                LocalHarvesters.datasetType(),
                datasetTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        datasetTypeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_dataset_types_returns_valid_response() {
        DatasetTypes datasetTypes =
                restClient.get().uri("/eu/dataset-types").retrieve().body(DatasetTypes.class);
        DatasetType first = datasetTypes.getDatasetTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/APROF", first.getUri());
        assertEquals("APROF", first.getCode());
        assertEquals("Application profile", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_dataset_type_by_code_returns_valid_response() {
        DatasetType datasetType =
                restClient.get().uri("/eu/dataset-types/NAL").retrieve().body(DatasetType.class);

        assertNotNull(datasetType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/NAL", datasetType.getUri());
        assertEquals("NAL", datasetType.getCode());
        assertEquals("Name authority list", datasetType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_all_translated_dataset_types_has_correct_value() {
        DatasetType hvdType =
                restClient.get().uri("/eu/dataset-types/HVD").retrieve().body(DatasetType.class);

        assertNotNull(hvdType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/HVD", hvdType.getUri());
        assertEquals("HVD", hvdType.getCode());
        assertEquals("High-value dataset", hvdType.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Datasett med høy verdi", hvdType.getLabel().get(Language.NORWEGIAN.code()));
        assertEquals("Datasett med høg verdi", hvdType.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
        assertEquals("Datasett med høy verdi", hvdType.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));

        DatasetType releaseType =
                restClient.get().uri("/eu/dataset-types/RELEASE").retrieve().body(DatasetType.class);

        assertNotNull(releaseType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/RELEASE", releaseType.getUri());
        assertEquals("RELEASE", releaseType.getCode());
        assertEquals("Release", releaseType.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Versjon", releaseType.getLabel().get(Language.NORWEGIAN.code()));
        assertEquals("Versjon", releaseType.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
        assertEquals("Versjon", releaseType.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));

        DatasetType statisticalType =
                restClient.get().uri("/eu/dataset-types/STATISTICAL").retrieve().body(DatasetType.class);

        assertNotNull(statisticalType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/STATISTICAL", statisticalType.getUri());
        assertEquals("STATISTICAL", statisticalType.getCode());
        assertEquals("Statistical data", statisticalType.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Statistiske data", statisticalType.getLabel().get(Language.NORWEGIAN.code()));
        assertEquals("Statistiske data", statisticalType.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
        assertEquals("Statistiske data", statisticalType.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));

        DatasetType syntheticType =
                restClient.get().uri("/eu/dataset-types/SYNTHETIC_DATA").retrieve().body(DatasetType.class);

        assertNotNull(syntheticType);
        assertEquals("http://publications.europa.eu/resource/authority/dataset-type/SYNTHETIC_DATA", syntheticType.getUri());
        assertEquals("SYNTHETIC_DATA", syntheticType.getCode());
        assertEquals("Synthetic data", syntheticType.getLabel().get(Language.ENGLISH.code()));
        assertEquals("Syntetiske data", syntheticType.getLabel().get(Language.NORWEGIAN.code()));
        assertEquals("Syntetiske data", syntheticType.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
        assertEquals("Syntetiske data", syntheticType.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_dataset_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/dataset-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(DatasetTypeControllerIntegrationTest.class.getClassLoader().getResource("dataset-types-translated.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
