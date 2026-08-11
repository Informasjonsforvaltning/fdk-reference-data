package no.fdk.referencedata.eu.frequency;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.FREQUENCIES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class FrequencyServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private FrequencyRepository frequencyRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        FrequencyService frequencyService = new FrequencyService(
                LocalHarvesters.frequency(),
                frequencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        frequencyService.harvestAndSave();
    }

    @Test
    public void test_if_harvest_persists_frequencies() {
        FrequencyService frequencyService = new FrequencyService(
                LocalHarvesters.frequency(),
                frequencyRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        frequencyService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        frequencyRepository.findAll().forEach(frequency -> counter.incrementAndGet());
        assertEquals(FREQUENCIES_SIZE, counter.get());

        final Frequency first = frequencyRepository.findById("http://publications.europa.eu/resource/authority/frequency/ANNUAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/frequency/ANNUAL", first.getUri());
        assertEquals("ANNUAL", first.getCode());
        assertEquals("annual", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        FrequencyRepository frequencyRepositorySpy = spy(this.frequencyRepository);

        Frequency frequency = Frequency.builder()
                .uri("http://uri.no")
                .code("FREQUENCY")
                .label(Map.of("en", "My frequency"))
                .build();
        frequencyRepositorySpy.save(frequency);

        long count = frequencyRepositorySpy.count();
        assertTrue(count > 0);

        when(frequencyRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new FrequencyService(
                LocalHarvesters.frequency(),
                frequencyRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, frequencyRepositorySpy.count());
    }
}
