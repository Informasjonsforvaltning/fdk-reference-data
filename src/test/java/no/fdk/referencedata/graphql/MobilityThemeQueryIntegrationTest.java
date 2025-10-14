package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.mobility.theme.LocalMobilityThemeHarvester;
import no.fdk.referencedata.mobility.theme.MobilityTheme;
import no.fdk.referencedata.mobility.theme.MobilityThemeRepository;
import no.fdk.referencedata.mobility.theme.MobilityThemeService;
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
                "wiremock.host=dummy",
                "wiremock.port=0"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class MobilityThemeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityThemeRepository mobilityThemeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private GraphQlTester graphQlTester;

    @BeforeEach
    public void setup() {
        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.0.0"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityThemeService.harvestAndSave(false);
    }

    @Test
    void test_if_mobility_themes_query_returns_all_mobility_themes() {
        List<MobilityTheme> result = graphQlTester.documentName("mobility-themes")
                .execute()
                .path("$['data']['mobilityThemes']")
                .entityList(MobilityTheme.class)
                .get();

        assertEquals(123, result.size());

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

    @Test
    void test_if_mobility_theme_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("mobility-theme-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['mobilityThemeByCode']")
                .valueIsNull();
    }

}
