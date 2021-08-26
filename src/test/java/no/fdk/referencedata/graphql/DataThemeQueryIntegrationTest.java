package no.fdk.referencedata.graphql;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import no.fdk.referencedata.eu.datatheme.DataThemeService;
import no.fdk.referencedata.eu.datatheme.LocalDataThemeHarvester;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
class DataThemeQueryIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private GraphQLTestTemplate template;

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @BeforeEach
    public void setup() {
        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("1"),
                dataThemeRepository,
                harvestSettingsRepository);

        dataThemeService.harvestAndSave();
    }

    @Test
    void test_if_data_themes_query_returns_all_data_themes() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/data-themes.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", response.get("$['data']['dataThemes'][0]['uri']"));
        assertEquals("AGRI", response.get("$['data']['dataThemes'][0]['code']"));
        assertEquals("Agriculture, fisheries, forestry and food", response.get("$['data']['dataThemes'][0]['label']['en']"));
    }

    @Test
    void test_if_data_theme_by_code_aac_query_returns_econ_data_theme() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/data-theme-by-code.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/ECON", response.get("$['data']['dataThemeByCode']['uri']"));
        assertEquals("ECON", response.get("$['data']['dataThemeByCode']['code']"));
        assertEquals("Economy and finance", response.get("$['data']['dataThemeByCode']['label']['en']"));
    }

    @Test
    void test_if_data_theme_by_code_unknown_query_returns_null() throws IOException {
        GraphQLResponse response = template.postForResource("graphql/data-theme-by-code-unknown.graphql");
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNull(response.get("$['data']['dataThemeByCode']"));
    }

}
