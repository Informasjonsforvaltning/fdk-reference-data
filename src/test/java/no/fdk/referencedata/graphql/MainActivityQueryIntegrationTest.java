package no.fdk.referencedata.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.mainactivity.LocalMainActivityHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import no.fdk.referencedata.eu.mainactivity.MainActivityService;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "scheduling.enabled=false",
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class MainActivityQueryIntegrationTest extends AbstractContainerTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MainActivityRepository mainActivityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private GraphQLTestTemplate template;

    @BeforeEach
    public void setup() {
        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("1"),
                mainActivityRepository,
                harvestSettingsRepository);

        mainActivityService.harvestAndSave(false);
    }

    @Test
    void test_if_main_activities_query_returns_all_main_activities() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/main-activities.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/OP_DATPRO", response.get("$['data']['mainActivities'][0]['uri']"));
        assertEquals("OP_DATPRO", response.get("$['data']['mainActivities'][0]['code']"));
        assertEquals("Provisional data", response.get("$['data']['mainActivities'][0]['label']['en']"));
    }

    @Test
    void test_if_main_activity_by_code_public_query_returns_public_main_activity() throws IOException {
        GraphQLResponse response = template.perform("graphql/main-activity-by-code.graphql",
                mapper.valueToTree(Map.of("code", "airport")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/airport", response.get("$['data']['mainActivityByCode']['uri']"));
        assertEquals("airport", response.get("$['data']['mainActivityByCode']['code']"));
        assertEquals("Airport-related activities", response.get("$['data']['mainActivityByCode']['label']['en']"));
    }

    @Test
    void test_if_main_activity_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.perform("graphql/main-activity-by-code.graphql",
                mapper.valueToTree(Map.of("code", "unknown")));
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['mainActivityByCode']"));
    }

}
