package no.fdk.referencedata.eu.filetype;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class FileTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_filetypes() {
        FileTypeService fileTypeService = new FileTypeService(
                LocalHarvesters.fileType(),
                fileTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        fileTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        fileTypeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(198, counter.get());

        final FileType first = fileTypeRepository.findById("http://publications.europa.eu/resource/authority/file-type/7Z").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/file-type/7Z", first.getUri());
        assertEquals("7Z", first.getCode());
        assertEquals("application/x-7z-compressed", first.getMediaType());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FileTypeRepository fileTypeRepositorySpy = spy(fileTypeRepository);

        fileTypeRepositorySpy.save(FileType.builder()
                .uri("http://uri.no")
                .code("FIL")
                .mediaType("text/fil")
                .build());

        long count = fileTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(fileTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new FileTypeService(
                LocalHarvesters.fileType(),
                fileTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, fileTypeRepositorySpy.count());
    }
}
