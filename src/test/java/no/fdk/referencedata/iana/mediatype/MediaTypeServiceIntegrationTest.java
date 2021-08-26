package no.fdk.referencedata.iana.mediatype;

import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
public class MediaTypeServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    @Test
    public void test_if_harvest_persists_mediatypes() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository);

        mediaTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        mediaTypeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(1441, counter.get());

        final MediaType first = mediaTypeRepository.findById("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec").orElseThrow();
        assertEquals("https://www.iana.org/assignments/media-types/application/1d-interleaved-parityfec", first.getUri());
        assertEquals("1d-interleaved-parityfec", first.getName());
        assertEquals("application", first.getType());
        assertEquals("1d-interleaved-parityfec", first.getSubType());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MediaTypeRepository mediaTypeRepositorySpy = Mockito.spy(mediaTypeRepository);

        mediaTypeRepositorySpy.save(MediaType.builder()
                .uri("http://uri.no")
                .name("NAME")
                .type("text")
                .subType("fil")
                .build());

        long count = mediaTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(mediaTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepositorySpy);

        assertEquals(count, mediaTypeRepositorySpy.count());
    }
}
