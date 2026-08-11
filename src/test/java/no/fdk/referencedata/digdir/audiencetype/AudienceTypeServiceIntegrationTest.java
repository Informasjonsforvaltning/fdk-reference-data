package no.fdk.referencedata.digdir.audiencetype;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class AudienceTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private AudienceTypeRepository audienceTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_audience_types() {
        AudienceTypeService audienceTypeService = new AudienceTypeService(
                LocalHarvesters.audienceType(),
                audienceTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        audienceTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        audienceTypeRepository.findAll().forEach(audienceType -> counter.incrementAndGet());
        assertEquals(2, counter.get());

        final AudienceType first = audienceTypeRepository.findById("https://data.norge.no/vocabulary/audience-type#public").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/audience-type#public", first.getUri());
        assertEquals("public", first.getCode());
        assertEquals("public", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("allmenta", first.getLabel().get(Language.NORWEGIAN_NYNORSK.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        AudienceTypeRepository audienceTypeRepositorySpy = spy(this.audienceTypeRepository);

        AudienceType audienceType = AudienceType.builder()
                .uri("http://uri.no")
                .code("AUDIENCE_TYPE")
                .label(Map.of("en", "My audience"))
                .build();
        audienceTypeRepositorySpy.save(audienceType);

        long count = audienceTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(audienceTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new AudienceTypeService(
                LocalHarvesters.audienceType(),
                audienceTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, audienceTypeRepositorySpy.count());
    }
}
