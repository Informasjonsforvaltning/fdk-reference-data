package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.eu.mainactivity.LocalMainActivityHarvester;
import no.fdk.referencedata.eu.mainactivity.MainActivity;
import no.fdk.referencedata.eu.mainactivity.MainActivityRepository;
import no.fdk.referencedata.eu.mainactivity.MainActivityService;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class MainActivityQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("1"),
                mainActivityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mainActivityService.harvestAndSave(false);
    }

    @Test
    void test_if_main_activities_query_returns_all_main_activities() {
        List<MainActivity> result = graphQlTester.documentName("main-activities")
                .execute()
                .path("$['data']['mainActivities']")
                .entityList(MainActivity.class)
                .get();

        assertEquals(20, result.size());

        MainActivity mainActivity = result.get(0);

        assertEquals("http://publications.europa.eu/resource/authority/main-activity/airport", mainActivity.getUri());
        assertEquals("airport", mainActivity.getCode());
        assertEquals("Airport-related activities", mainActivity.getLabel().get("en"));
    }

    @Test
    void test_if_main_activity_by_code_public_query_returns_public_main_activity() {
        MainActivity result = graphQlTester.documentName("main-activity-by-code")
                .variable("code", "airport")
                .execute()
                .path("$['data']['mainActivityByCode']")
                .entity(MainActivity.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/main-activity/airport", result.getUri());
        assertEquals("airport", result.getCode());
        assertEquals("Airport-related activities", result.getLabel().get("en"));
    }

    @Test
    void test_if_main_activity_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("main-activity-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['mainActivityByCode']")
                .valueIsNull();
    }

}
