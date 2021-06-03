package no.fdk.referencedata.mediatype;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MediaTypeHarvesterTest {

    @Test
    public void test_fetch_mediatypes() {
        MediaTypeHarvester mediaTypeHarvester = new LocalMediaTypeHarvester();

        Resource firstResource = mediaTypeHarvester.getMediaTypesSources().blockFirst();
        assertNotNull(firstResource);
        assertEquals("mediatypes-test.csv", firstResource.getFilename());

        List<MediaType> mediaTypes = mediaTypeHarvester.harvestMediaTypes().collectList().block();
        assertNotNull(mediaTypes);
        assertEquals(1441, mediaTypes.size());

        MediaType first = mediaTypes.get(0);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }

}
