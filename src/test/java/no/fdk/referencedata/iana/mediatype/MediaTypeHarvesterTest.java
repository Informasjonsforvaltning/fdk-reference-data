package no.fdk.referencedata.iana.mediatype;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class MediaTypeHarvesterTest {

    @Test
    public void test_fetch_mediatypes() {
        MediaTypeHarvester mediaTypeHarvester = new LocalMediaTypeHarvester();

        IanaSource firstSource = mediaTypeHarvester.getSources().blockFirst();
        assertNotNull(firstSource);
        assertEquals("mediatypes-test.csv", firstSource.getResource().getFilename());

        List<MediaType> mediaTypes = mediaTypeHarvester.harvest().collectList().block();
        assertNotNull(mediaTypes);
        assertEquals(1441, mediaTypes.size());

        MediaType first = mediaTypes.get(0);
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }

}
