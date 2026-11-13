package no.fdk.referencedata.digdir.highvaluedatasettype;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class HighValueDatasetTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private HighValueDatasetTypeRepository highValueDatasetTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_high_value_dataset_types() {
        HighValueDatasetTypeService highValueDatasetTypeService = new HighValueDatasetTypeService(
                LocalHarvesters.highValueDatasetType(),
                highValueDatasetTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        highValueDatasetTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        highValueDatasetTypeRepository.findAll().forEach(highValueDatasetType -> counter.incrementAndGet());
        assertEquals(3, counter.get());

        final HighValueDatasetType first = highValueDatasetTypeRepository.findById("https://data.norge.no/vocabulary/high-value-dataset-type#national-basic-data").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/high-value-dataset-type#national-basic-data", first.getUri());
        assertEquals("national-basic-data", first.getCode());
        assertEquals("National basic data", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        HighValueDatasetTypeRepository highValueDatasetTypeRepositorySpy = spy(this.highValueDatasetTypeRepository);

        HighValueDatasetType highValueDatasetType = HighValueDatasetType.builder()
                .uri("http://uri.no")
                .code("HIGH_VALUE_DATASET_TYPE")
                .label(Map.of("en", "My high-value dataset type"))
                .build();
        highValueDatasetTypeRepositorySpy.save(highValueDatasetType);

        long count = highValueDatasetTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(highValueDatasetTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new HighValueDatasetTypeService(
                LocalHarvesters.highValueDatasetType(),
                highValueDatasetTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, highValueDatasetTypeRepositorySpy.count());
    }
}
