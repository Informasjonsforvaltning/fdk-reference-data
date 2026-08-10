package no.fdk.referencedata.eu.mainactivity;

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
public class MainActivityControllerIntegrationTest extends AbstractContainerTest {

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

        HarvestTestSupport.harvest(registry, "main-activity");
    }

    @Test
    public void test_if_get_all_access_rights_returns_valid_response() {
        MainActivities mainActivities =
                restClient.get().uri("/eu/main-activities").retrieve().body(MainActivities.class);
        MainActivity first = mainActivities.getMainActivities().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/airport", first.getUri());
        assertEquals("airport", first.getCode());
        assertEquals("Airport-related activities", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_access_right_by_code_returns_valid_response() {
        MainActivity mainActivity =
                restClient.get().uri("/eu/main-activities/health").retrieve().body(MainActivity.class);

        assertNotNull(mainActivity);
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/health", mainActivity.getUri());
        assertEquals("health", mainActivity.getCode());
        assertEquals("Health", mainActivity.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_main_activities_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/eu/main-activities", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MainActivityControllerIntegrationTest.class.getClassLoader().getResource("main-activity-sparql-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
