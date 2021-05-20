package no.fdk.referencedata.mediatype;

import no.fdk.referencedata.redis.AbstractRedisContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MediaTypeControllerIntegrationTest extends AbstractRedisContainerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository);

        mediaTypeService.harvestAndSaveMediaTypes();
    }

    @Test
    public void test_if_get_all_mediatypes_returns_valid_response() {
        MediaTypes mediaTypes =
                this.restTemplate.getForObject("http://localhost:" + port + "/media-types", MediaTypes.class);

        assertEquals(1440, mediaTypes.getMediaTypes().size());

        MediaType first = mediaTypes.getMediaTypes().get(0);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }

    @Test
    public void test_if_get_single_mediatype_returns_valid_response() {
        MediaType mediaType =
                this.restTemplate.getForObject("http://localhost:" + port + "/media-types/https%3A%2F%2Fwww.iana.org%2Fassignments%2Fmedia-types%2Fapplication%2F1d-interleaved-parityfec", MediaType.class);

        assertNotNull(mediaType);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", mediaType.getUri());
        assertEquals("1d-interleaved-parityfec", mediaType.getName());
        assertEquals("application", mediaType.getType());
        assertEquals("1d-interleaved-parityfec", mediaType.getSubType());
    }
}