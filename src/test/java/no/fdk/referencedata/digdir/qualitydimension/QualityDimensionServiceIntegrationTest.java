package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.digdir.qualitydimension.LocalQualityDimensionHarvester.QUALITY_DIMENSIONS_SIZE;
import static no.fdk.referencedata.settings.Settings.QUALITY_DIMENSION;
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

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @BeforeEach
    public void setup() {
        qualityDimensionRepository.deleteAll();
        harvestSettingsRepository.deleteAll();
    }

    @Test
    public void test_if_harvest_persists_quality_dimensions() {
        QualityDimensionService qualityDimensionService = new QualityDimensionService(
                new LocalQualityDimensionHarvester("2023-01-30"),
                qualityDimensionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        qualityDimensionService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        qualityDimensionRepository.findAll().forEach(qualityDimension -> counter.incrementAndGet());
        assertEquals(QUALITY_DIMENSIONS_SIZE, counter.get());

        final QualityDimension first = qualityDimensionRepository.findById("https://data.norge.no/vocabulary/quality-dimension#accuracy").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/quality-dimension#accuracy", first.getUri());
        assertEquals("accuracy", first.getCode());
        assertEquals("accuracy", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        QualityDimensionService qualityDimensionService = new QualityDimensionService(
                new LocalQualityDimensionHarvester("2023-01-31"),
                qualityDimensionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        qualityDimensionService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(QUALITY_DIMENSION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-01-31", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        qualityDimensionService = new QualityDimensionService(
                new LocalQualityDimensionHarvester("2023-02-01"),
                qualityDimensionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        qualityDimensionService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(QUALITY_DIMENSION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-02-01", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        qualityDimensionService = new QualityDimensionService(
                new LocalQualityDimensionHarvester("2023-01-29"),
                qualityDimensionRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        qualityDimensionService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(QUALITY_DIMENSION.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2023-02-01", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        QualityDimensionService qualityDimensionService = new QualityDimensionService(
                new LocalQualityDimensionHarvester("2023-01-30"),
                qualityDimensionRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, qualityDimensionRepositorySpy.count());
    }
}
