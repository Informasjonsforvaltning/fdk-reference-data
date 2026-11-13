package no.fdk.referencedata.digdir.highvaluedatasettype;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class HighValueDatasetTypeControllerIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        HarvestTestSupport.harvest(registry, "high-value-dataset-type");
    }

    @Test
    public void test_if_get_all_high_value_dataset_types_returns_valid_response() {
        HighValueDatasetTypes highValueDatasetTypes =
                restClient.get().uri("/digdir/high-value-dataset-types").retrieve().body(HighValueDatasetTypes.class);
        HighValueDatasetType first = highValueDatasetTypes.getHighValueDatasetTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#eu-hvd", first.getUri());
        assertEquals("eu-hvd", first.getCode());
        assertEquals("High-value dataset (EU)", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_service_output_type_by_code_returns_valid_response() {
        HighValueDatasetType highValueDatasetType =
                restClient.get().uri("/digdir/high-value-dataset-types/national-hvd").retrieve().body(HighValueDatasetType.class);

        assertNotNull(highValueDatasetType);
        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#national-hvd", highValueDatasetType.getUri());
        assertEquals("national-hvd", highValueDatasetType.getCode());
        assertEquals("National high-value dataset", highValueDatasetType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_high_value_dataset_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/high-value-dataset-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(HighValueDatasetTypeControllerIntegrationTest.class.getClassLoader().getResource("high-value-dataset-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
