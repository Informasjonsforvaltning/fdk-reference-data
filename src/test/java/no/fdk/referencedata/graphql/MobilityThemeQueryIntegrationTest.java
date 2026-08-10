package no.fdk.referencedata.graphql;

import no.fdk.referencedata.HarvestTestSupport;
import no.fdk.referencedata.core.ReferenceDataRegistry;
import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.mobility.theme.MobilityTheme;
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
class MobilityThemeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ReferenceDataRegistry registry;

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        HarvestTestSupport.harvest(registry, "mobility-theme");
    }

    @Test
    void test_if_mobility_themes_query_returns_all_mobility_themes() {
        List<MobilityTheme> result = graphQlTester.documentName("mobility-themes")
                .execute()
                .path("$['data']['mobilityThemes']")
                .entityList(MobilityTheme.class)
                .get();
        MobilityTheme theme = result.get(0);

        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/accesibility-information-for-vehicles", theme.getUri());
        assertEquals("accesibility-information-for-vehicles", theme.getCode());
        assertEquals("Accesibility information for vehicles", theme.getLabel().get("en"));
    }

    @Test
    void test_if_mobility_theme_by_code_public_query_returns_correct_theme() {
        MobilityTheme result = graphQlTester.documentName("mobility-theme-by-code")
                .variable("code", "junctions")
                .execute()
                .path("$['data']['mobilityThemeByCode']")
                .entity(MobilityTheme.class)
                .get();

        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/junctions", result.getUri());
        assertEquals("junctions", result.getCode());
        assertEquals("Junctions", result.getLabel().get("en"));
    }

}
