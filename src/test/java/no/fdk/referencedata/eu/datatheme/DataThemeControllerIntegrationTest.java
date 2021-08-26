package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DataThemeControllerIntegrationTest extends AbstractMongoDbContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("1"),
                dataThemeRepository,
                harvestSettingsRepository);

        dataThemeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_datathemes_returns_valid_response() {
        DataThemes dataThemes =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/data-themes", DataThemes.class);

        assertEquals(14, dataThemes.getDataThemes().size());

        DataTheme first = dataThemes.getDataThemes().get(0);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", first.getUri());
        assertEquals("AGRI", first.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_datatheme_by_code_returns_valid_response() {
        DataTheme dataTheme =
                this.restTemplate.getForObject("http://localhost:" + port + "/eu/data-themes/AGRI", DataTheme.class);

        assertNotNull(dataTheme);
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", dataTheme.getUri());
        assertEquals("AGRI", dataTheme.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", dataTheme.getLabel().get(Language.ENGLISH.code()));
    }
}
