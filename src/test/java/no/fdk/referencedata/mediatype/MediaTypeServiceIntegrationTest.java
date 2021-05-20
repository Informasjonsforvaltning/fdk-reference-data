package no.fdk.referencedata.mediatype;

import no.fdk.referencedata.redis.AbstractRedisContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(properties = { "scheduling.enabled=false" })
public class MediaTypeServiceIntegrationTest extends AbstractRedisContainerTest {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Test
    public void test_if_harvest_persists_mediatypes() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository);

        mediaTypeService.harvestAndSaveMediaTypes();

        final AtomicInteger counter = new AtomicInteger();
        mediaTypeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(1440, counter.get());

        final MediaType first = mediaTypeRepository.findById("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec").orElseThrow();
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }
}
