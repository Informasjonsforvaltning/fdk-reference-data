package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.eu.eurovoc.EuroVocWriter;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester.EUROVOCS_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class EuroVocServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private EuroVocRepository euroVocRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_eurovoc() {
        EuroVocService euroVocService = new EuroVocService(
                new LocalEuroVocHarvester(),
                euroVocRepository,
                rdfSourceRepository,
                new EuroVocWriter(euroVocRepository, rdfSourceRepository));

        euroVocService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        euroVocRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(EUROVOCS_SIZE, counter.get());

        final EuroVoc euroVoc337 = euroVocRepository.findById("http://eurovoc.europa.eu/337").orElseThrow();
        assertEquals("http://eurovoc.europa.eu/337", euroVoc337.getUri());
        assertEquals("337", euroVoc337.getCode());
        assertEquals("regions of Denmark", euroVoc337.getLabel().get(Language.ENGLISH.code()));
        assertTrue(euroVoc337.getChildren().contains(URI.create("http://eurovoc.europa.eu/1")));
        assertEquals(21, euroVoc337.getChildren().size());
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        EuroVocRepository EuroVocRepositorySpy = spy(this.euroVocRepository);

        EuroVocRepositorySpy.save(EuroVoc.builder()
                .uri("http://uri.no")
                .code("1111")
                .label(Map.of("en", "My EuroVoc"))
                .build());

        long count = EuroVocRepositorySpy.count();
        assertTrue(count > 0);

        when(EuroVocRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new EuroVocService(
                new LocalEuroVocHarvester(),
                EuroVocRepositorySpy,
                rdfSourceRepository,
                new EuroVocWriter(euroVocRepository, rdfSourceRepository));

        assertEquals(count, EuroVocRepositorySpy.count());
    }
}
