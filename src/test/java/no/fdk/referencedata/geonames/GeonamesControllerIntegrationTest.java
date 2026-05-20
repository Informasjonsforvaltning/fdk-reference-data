package no.fdk.referencedata.geonames;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class GeonamesControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GeonamesFylkeRepository geonamesFylkeRepository;

    @Autowired
    private GeonamesKommuneRepository geonamesKommuneRepository;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @Value("${wiremock.host}")
    private String wiremockHost;

    @Value("${wiremock.port}")
    private String wiremockPort;

    private RestClient restClient;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        GeonamesService geonamesService = new GeonamesService(
                new LocalGeonamesHarvester(wiremockHost, wiremockPort),
                geonamesFylkeRepository,
                geonamesKommuneRepository,
                rdfSourceRepository,
                new GeonamesWriter(geonamesFylkeRepository, geonamesKommuneRepository, rdfSourceRepository));

        geonamesService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_fylker_returns_valid_response() {
        GeonamesFylker response = restClient.get().uri("/geonames/fylker").retrieve().body(GeonamesFylker.class);

        assertNotNull(response);
        assertEquals(2, response.getFylker().size());

        GeonamesFylke first = response.getFylker().get(0);
        assertEquals("7626836", first.getGeonameId());
        assertEquals("Agder", first.getName());
    }

    @Test
    public void test_if_get_fylke_by_geoname_id_returns_valid_response() {
        GeonamesFylke response = restClient.get().uri("/geonames/fylker/3162656").retrieve().body(GeonamesFylke.class);

        assertNotNull(response);
        assertEquals("3162656", response.getGeonameId());
        assertEquals("Vestland", response.getName());
    }

    @Test
    public void test_if_get_all_kommuner_returns_valid_response() {
        GeonamesKommuner response = restClient.get().uri("/geonames/kommuner").retrieve().body(GeonamesKommuner.class);

        assertNotNull(response);
        assertEquals(4, response.getKommuner().size());
    }

    @Test
    public void test_if_get_kommune_by_geoname_id_returns_valid_response() {
        GeonamesKommune response = restClient.get().uri("/geonames/kommuner/7626837").retrieve().body(GeonamesKommune.class);

        assertNotNull(response);
        assertEquals("7626837", response.getGeonameId());
        assertEquals("Kristiansand", response.getName());
        assertEquals("7626836", response.getFylkeGeonameId());
    }

    @Test
    public void test_if_get_kommuner_for_fylke_returns_valid_response() {
        GeonamesKommuner response = restClient.get().uri("/geonames/fylker/7626836/kommuner").retrieve().body(GeonamesKommuner.class);

        assertNotNull(response);
        assertEquals(2, response.getKommuner().size());

        GeonamesKommune first = response.getKommuner().get(0);
        assertEquals("7626838", first.getGeonameId());
        assertEquals("Arendal", first.getName());
    }

    @Test
    public void test_if_post_geonames_fails_without_api_key() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/geonames/fylker")
                .headers(h -> h.addAll(headers))
                .exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void test_if_post_geonames_triggers_harvest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/geonames/fylker")
                .headers(h -> h.addAll(headers))
                .exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, geonamesFylkeRepository.count());
        assertEquals(4, geonamesKommuneRepository.count());
    }

    @Test
    public void test_geonames_rdf_response() {
        String rdf = restClient.get().uri("/geonames")
                .header("Accept", "text/turtle")
                .retrieve()
                .body(String.class);

        assertNotNull(rdf);
        assertTrue(rdf.contains("sws.geonames.org"));
    }
}
