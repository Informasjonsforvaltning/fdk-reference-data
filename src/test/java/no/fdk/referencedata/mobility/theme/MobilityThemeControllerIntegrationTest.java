package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class MobilityThemeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MobilityThemeRepository mobilityThemeRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                LocalHarvesters.mobilityTheme(),
                mobilityThemeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        mobilityThemeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_mobility_themes_returns_valid_response() {
        MobilityThemes mobilityThemes =
                restClient.get()
                        .uri("/mobility/themes")
                        .retrieve()
                        .body(MobilityThemes.class);
        MobilityTheme first = mobilityThemes.getMobilityThemes().get(0);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/accesibility-information-for-vehicles", first.getUri());
        assertEquals("accesibility-information-for-vehicles", first.getCode());
        assertEquals("Accesibility information for vehicles", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_get_mobility_theme_by_code_returns_valid_response() {
        MobilityTheme theme =
                restClient.get()
                        .uri("/mobility/themes/speed-limits")
                        .retrieve()
                        .body(MobilityTheme.class);

        assertNotNull(theme);
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/speed-limits", theme.getUri());
        assertEquals("speed-limits", theme.getCode());
        assertEquals("Speed limits", theme.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_mobility_themes_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/mobility/themes", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MobilityThemeControllerIntegrationTest.class.getClassLoader().getResource("mobility-themes.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
