package no.fdk.referencedata.eu.datatheme;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.DATA_THEME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DataThemeServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_datathemes() {
        DataThemeService fileTypeService = new DataThemeService(
                new LocalDataThemeHarvester("20200923-0"),
                dataThemeRepository,
                harvestSettingsRepository);

        fileTypeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        dataThemeRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(14, counter.get());

        final DataTheme first = dataThemeRepository.findById("http://publications.europa.eu/resource/authority/data-theme/AGRI").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/AGRI", first.getUri());
        assertEquals("AGRI", first.getCode());
        assertEquals("Agriculture, fisheries, forestry and food", first.getLabel().get(Language.ENGLISH.code()));
        assertEquals("http://publications.europa.eu/resource/authority/data-theme", first.getConceptSchema().getUri());
        assertEquals("Data theme", first.getConceptSchema().getLabel().get(Language.ENGLISH.code()));
        assertEquals("20200923-0", first.getConceptSchema().getVersionNumber());
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("20200923-1"),
                dataThemeRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        dataThemeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(DATA_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("20200924-0"),
                dataThemeRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        dataThemeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(DATA_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("20200924-0"),
                dataThemeRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        dataThemeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(DATA_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        DataThemeRepository dataThemeRepositorySpy = spy(this.dataThemeRepository);

        dataThemeRepositorySpy.save(DataTheme.builder()
                .uri("http://uri.no")
                .code("THEME")
                .label(Map.of("en", "My theme"))
                .build());


        long count = dataThemeRepositorySpy.count();
        assertTrue(count > 0);

        when(dataThemeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        DataThemeService dataThemeService = new DataThemeService(
                new LocalDataThemeHarvester("20200924-2"),
                dataThemeRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, dataThemeRepositorySpy.count());
    }
}
