package no.fdk.referencedata.graphql;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester;
import no.fdk.referencedata.container.AbstractContainerTest;
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
        })
@AutoConfigureGraphQlTester
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
class DataThemeQueryIntegrationTest extends AbstractContainerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private DataThemeRepository dataThemeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("1"),
                dataThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        dataThemeService.harvestAndSave(false);
    }

    @Test
    void test_if_data_themes_query_returns_all_data_themes() {
        List<DataTheme> result = graphQlTester.documentName("data-themes")
                .execute()
                .path("$['data']['dataThemes']")
                .entityList(DataTheme.class)
                .get();

        Assertions.assertEquals(13, result.size());

        DataTheme dataTheme = result.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", dataTheme.getUri());
        assertEquals("AGRI", dataTheme.getCode());
        assertEquals("Jordbruk, fiskeri, skogbruk og mat", dataTheme.getLabel().get("no"));
        assertEquals("Jordbruk, fiskeri, skogbruk og mat", dataTheme.getLabel().get("nb"));
        assertEquals("Jordbruk, fiskeri, skogbruk og mat", dataTheme.getLabel().get("nn"));
        assertEquals("Agriculture, fisheries, forestry and food", dataTheme.getLabel().get("en"));
    }

    @Test
    void test_if_data_theme_by_code_aac_query_returns_econ_data_theme() {
        DataTheme result = graphQlTester.documentName("data-theme-by-code")
                .variable("code", "ECON")
                .execute()
                .path("$['data']['dataThemeByCode']")
                .entity(DataTheme.class)
                .get();

        assertEquals("http://publications.europa.eu/resource/authority/data-theme/ECON", result.getUri());
        assertEquals("ECON", result.getCode());
        assertEquals("Økonomi og finans", result.getLabel().get("no"));
        assertEquals("Økonomi og finans", result.getLabel().get("nb"));
        assertEquals("Økonomi og finans", result.getLabel().get("nn"));
        assertEquals("Economy and finance", result.getLabel().get("en"));
    }

    @Test
    void test_if_data_theme_by_code_unknown_query_returns_null() {
        graphQlTester.documentName("data-theme-by-code")
                .variable("code", "unknown")
                .execute()
                .path("$['data']['dataThemeByCode']")
                .valueIsNull();
    }

}
