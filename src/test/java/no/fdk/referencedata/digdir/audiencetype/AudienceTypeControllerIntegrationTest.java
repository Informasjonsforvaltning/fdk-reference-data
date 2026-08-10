package no.fdk.referencedata.digdir.audiencetype;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class AudienceTypeControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "audience-type");
    }

    @Test
    public void test_if_get_all_audience_types_returns_valid_response() {
        AudienceTypes audienceTypes =
                restClient.get().uri("/digdir/audience-types").retrieve().body(AudienceTypes.class);
        AudienceType first = audienceTypes.getAudienceTypes().get(0);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", first.getUri());
        assertEquals("public", first.getCode());
        assertEquals("public", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_audience_type_by_code_returns_valid_response() {
        AudienceType audienceType =
                restClient.get().uri("/digdir/audience-types/public").retrieve().body(AudienceType.class);

        assertNotNull(audienceType);
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", audienceType.getUri());
        assertEquals("public", audienceType.getCode());
        assertEquals("public", audienceType.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_audience_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/digdir/audience-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(AudienceTypeControllerIntegrationTest.class.getClassLoader().getResource("audience-type.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
