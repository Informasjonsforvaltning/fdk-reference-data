package no.fdk.referencedata.geonorge.administrativeenheter;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
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

import static no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester.ADMINISTRATIVE_ENHETER_SIZE;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key",
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class EnhetControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EnhetRepository enhetRepository;

    @Autowired
    private EnhetVariantRepository enhetVariantRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        EnhetService enhetService = new EnhetService(
                new LocalEnhetHarvester(),
                enhetRepository,
                enhetVariantRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        enhetService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_administrative_enheter_returns_valid_response() {
        Enheter enheter =
                restClient.get().uri("/geonorge/administrative-enheter").retrieve().body(Enheter.class);

        assertEquals(ADMINISTRATIVE_ENHETER_SIZE, enheter.getEnheter().size());

        Enhet first = enheter.getEnheter().get(0);
        assertEquals("https://data.geonorge.no/administrativeEnheter/fylke/id/173143173142", first.getUri());
        assertEquals("Troms og Finnmark", first.getName());
        assertEquals("173143173142", first.getCode());
    }

    @Test
    public void test_if_get_enhet_by_code_returns_valid_response() {
        Enhet norge =
                restClient.get().uri("/geonorge/administrative-enheter/173163").retrieve().body(Enhet.class);

        assertNotNull(norge);
        assertEquals("https://data.geonorge.no/administrativeEnheter/nasjon/id/173163", norge.getUri());
        assertEquals("Norge", norge.getName());
        assertEquals("173163", norge.getCode());
    }

    @Test
    public void test_kommuner_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/geonorge/administrative-enheter", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(EnhetControllerIntegrationTest.class.getClassLoader().getResource("administrative-enheter-result.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
