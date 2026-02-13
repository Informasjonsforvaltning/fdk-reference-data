package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester.HIGH_VALUE_CATEGORIES_SIZE;
import static no.fdk.referencedata.settings.Settings.HIGH_VALUE_CATEGORY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class HighValueCategoryServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private HighValueCategoryRepository highValueCategoryRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_high_value_categories() {
        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("20200923-0"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        highValueCategoryService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        highValueCategoryRepository.findAll().forEach(category -> counter.incrementAndGet());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, counter.get());

        final HighValueCategory first = highValueCategoryRepository.findById("http://data.europa.eu/bna/c_164e0bf5").orElseThrow();
        assertEquals("http://data.europa.eu/bna/c_164e0bf5", first.getUri());
        assertEquals("c_164e0bf5", first.getCode());
        assertEquals("Meteorological", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("20200923-1"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        highValueCategoryService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("20200924-0"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        highValueCategoryService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("20200924-0"),
                highValueCategoryRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        highValueCategoryService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(HIGH_VALUE_CATEGORY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        HighValueCategoryRepository highValueCategoryRepositorySpy = spy(this.highValueCategoryRepository);

        HighValueCategory category = HighValueCategory.builder()
                .uri("http://uri.no")
                .code("TEST_CATEGORY")
                .label(Map.of("en", "My category"))
                .build();
        highValueCategoryRepositorySpy.save(category);

        long count = highValueCategoryRepositorySpy.count();
        assertTrue(count > 0);

        when(highValueCategoryRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        HighValueCategoryService highValueCategoryService = new HighValueCategoryService(
                new LocalHighValueCategoryHarvester("20200924-2"),
                highValueCategoryRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, highValueCategoryRepositorySpy.count());
    }
}
