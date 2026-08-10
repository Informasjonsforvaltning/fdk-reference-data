package no.fdk.referencedata.iana.mediatype;

import no.fdk.referencedata.iana.mediatype.LocalMediaTypeHarvester;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MediaTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MediaTypeRepository mediaTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_mediatypes() {
        MediaTypeService mediaTypeService = new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

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

        new MediaTypeService(
                new LocalMediaTypeHarvester(),
                mediaTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, mediaTypeRepositorySpy.count());
    }
}
