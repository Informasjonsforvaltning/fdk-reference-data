package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.QUALITY_DIMENSIONS_SIZE;
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
public class QualityDimensionServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private QualityDimensionRepository qualityDimensionRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        qualityDimensionRepository.deleteAll();
    }

    @Test
    public void test_if_harvest_persists_quality_dimensions() {
        QualityDimensionService qualityDimensionService = new QualityDimensionService(
                LocalHarvesters.qualityDimension(),
                qualityDimensionRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        qualityDimensionService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        qualityDimensionRepository.findAll().forEach(qualityDimension -> counter.incrementAndGet());
        assertEquals(QUALITY_DIMENSIONS_SIZE, counter.get());

        final QualityDimension first = qualityDimensionRepository.findById("https://data.norge.no/vocabulary/quality-dimension#accuracy").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/quality-dimension#accuracy", first.getUri());
        assertEquals("accuracy", first.getCode());
        assertEquals("accuracy", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        QualityDimensionRepository qualityDimensionRepositorySpy = spy(this.qualityDimensionRepository);

        QualityDimension qualityDimension = QualityDimension.builder()
                .uri("http://uri.no")
                .code("QUALITY_DIMENSION")
                .label(Map.of("en", "My dimension"))
                .build();
        qualityDimensionRepositorySpy.save(qualityDimension);

        long count = qualityDimensionRepositorySpy.count();
        assertTrue(count > 0);

        when(qualityDimensionRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new QualityDimensionService(
                LocalHarvesters.qualityDimension(),
                qualityDimensionRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, qualityDimensionRepositorySpy.count());
    }
}
