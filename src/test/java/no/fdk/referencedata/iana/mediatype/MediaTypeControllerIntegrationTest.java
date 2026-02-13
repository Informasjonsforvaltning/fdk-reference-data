package no.fdk.referencedata.iana.mediatype;

import no.fdk.referencedata.LocalHarvesterConfiguration;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import no.fdk.referencedata.settings.Settings;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "scheduling.enabled=false",
            "application.apiKey=my-api-key"
        })
@Import(LocalHarvesterConfiguration.class)
@ActiveProfiles("test")
public class MediaTypeControllerIntegrationTest extends AbstractContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private RestClient restClient;

    @Autowired
    private RDFSourceRepository rdfSourceRepository;

    @BeforeEach
    public void setup() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();

        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mediaTypeService.harvestAndSave();
    }

    @Test
    public void test_if_get_all_mediatypes_returns_valid_response() {
        MediaTypes mediaTypes =
                restClient.get().uri("/iana/media-types").retrieve().body(MediaTypes.class);

        assertEquals(1441, mediaTypes.getMediaTypes().size());

        MediaType first = mediaTypes.getMediaTypes().get(0);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }

    @Test
    public void test_if_get_mediatypes_by_type_returns_valid_response() {
        MediaTypes mediaTypes =
                restClient.get().uri("/iana/media-types/text").retrieve().body(MediaTypes.class);

        assertEquals(1, mediaTypes.getMediaTypes().size());

        MediaType first = mediaTypes.getMediaTypes().get(0);
        assertEquals("https://www.iana.org/assignments/media-types/text/plain", first.getUri());
        assertEquals("plain", first.getName());
        assertEquals("text", first.getType());
        assertEquals("plain", first.getSubType());
    }

    @Test
    public void test_if_get_mediatype_by_type_and_subtype_returns_valid_response() {
        MediaType mediaType =
                restClient.get().uri("/iana/media-types/application/1d-interleaved-parityfec").retrieve().body(MediaType.class);

        assertNotNull(mediaType);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", mediaType.getUri());
        assertEquals("1d-interleaved-parityfec", mediaType.getName());
        assertEquals("application", mediaType.getType());
        assertEquals("1d-interleaved-parityfec", mediaType.getSubType());
    }

    @Test
    public void test_if_post_media_types_fails_without_api_key() {
        assertEquals(1441, mediaTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name()).orElseThrow();
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "");
        ResponseEntity<Void> response = restClient.post().uri("/iana/media-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(1441, mediaTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name()).orElseThrow();
        assertEquals(harvestSettingsAfter.getLatestHarvestDate(), harvestSettingsBefore.getLatestHarvestDate());
    }

    @Test
    public void test_if_post_media_types_executes_a_force_update() {
        assertEquals(1441, mediaTypeRepository.count());

        HarvestSettings harvestSettingsBefore = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name()).orElseThrow();
        assertTrue(harvestSettingsBefore.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-KEY", "my-api-key");
        ResponseEntity<Void> response = restClient.post().uri("/iana/media-types")
                .headers(h -> h.addAll(headers)).exchange((request, clientResponse) -> ResponseEntity.status(clientResponse.getStatusCode()).build());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1441, mediaTypeRepository.count());

        HarvestSettings harvestSettingsAfter = harvestSettingsRepository.findById(Settings.MEDIA_TYPE.name()).orElseThrow();
        assertTrue(harvestSettingsAfter.getLatestHarvestDate().isAfter(harvestSettingsBefore.getLatestHarvestDate()));
    }

    @Test
    public void test_media_types_rdf_response() {
        Model rdfResponse = RDFDataMgr.loadModel("http://localhost:" + port + "/iana/media-types", Lang.TURTLE);
        Model expectedResponse = ModelFactory.createDefaultModel().read(String.valueOf(MediaTypeControllerIntegrationTest.class.getClassLoader().getResource("media-types.ttl")));

        assertTrue(rdfResponse.isIsomorphicWith(expectedResponse));
    }
}
