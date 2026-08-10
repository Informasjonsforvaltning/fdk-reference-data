package no.fdk.referencedata.digdir.qualitydimension;

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
public class QualityDimensionControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "quality-dimension");
    }

    @Test
    public void test_if_get_all_quality_dimensions_returns_valid_response() {
        QualityDimensions qualityDimensions =
                restClient.get().uri("/digdir/quality-dimensions").retrieve().body(QualityDimensions.class);
        QualityDimension first = qualityDimensions.getQualityDimensions().get(0);
        assertEquals("https://data.norge.no/vocabulary/quality-dimension#accuracy", first.getUri());
        assertEquals("accuracy", first.getCode());
        assertEquals("accuracy", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_quality_dimension_by_code_returns_valid_response() {
        QualityDimension qualityDimension =
                restClient.get().uri("/digdir/quality-dimensions/completeness").retrieve().body(QualityDimension.class);

        assertNotNull(qualityDimension);
        assertEquals("https://data.norge.no/vocabulary/quality-dimension#completeness", qualityDimension.getUri());
        assertEquals("completeness", qualityDimension.getCode());
        assertEquals("completeness", qualityDimension.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_quality_dimensions_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/quality-dimensions", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(QualityDimensionControllerIntegrationTest.class.getClassLoader().getResource("quality-dimension.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
