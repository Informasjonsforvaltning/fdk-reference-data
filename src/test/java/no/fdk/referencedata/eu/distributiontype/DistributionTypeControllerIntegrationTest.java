package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
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
public class DistributionTypeControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "distribution-type");
    }

    @Test
    public void test_if_get_all_distribution_types_returns_valid_response() {
        DistributionTypes distributionTypes =
                restClient.get().uri("/eu/distribution-types").retrieve().body(DistributionTypes.class);
        DistributionType first = distributionTypes.getDistributionTypes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", first.getUri());
        assertEquals("DOWNLOADABLE_FILE", first.getCode());
        assertEquals("Downloadable file", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_distribution_type_by_code_returns_valid_response() {
        DistributionType distributionType =
                restClient.get().uri("/eu/distribution-types/DOWNLOADABLE_FILE").retrieve().body(DistributionType.class);

        assertNotNull(distributionType);
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", distributionType.getUri());
        assertEquals("DOWNLOADABLE_FILE", distributionType.getCode());
        assertEquals("Downloadable file", distributionType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_distribution_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/distribution-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(DistributionTypeControllerIntegrationTest.class.getClassLoader().getResource("distribution-types-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
