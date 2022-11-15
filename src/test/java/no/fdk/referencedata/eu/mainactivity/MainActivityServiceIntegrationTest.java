package no.fdk.referencedata.eu.mainactivity;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.MAIN_ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MainActivityServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_datathemes() {
        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("123"),
                mainActivityRepository,
                harvestSettingsRepository);

        mainActivityService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        mainActivityRepository.findAll().forEach(activity -> counter.incrementAndGet());
        assertEquals(21, counter.get());

        final MainActivity first = mainActivityRepository.findById("http://publications.europa.eu/resource/authority/main-activity/hc-am").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/hc-am", first.getUri());
        assertEquals("hc-am", first.getCode());
        assertEquals("Housing and community amenities", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("123-0"),
                mainActivityRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        mainActivityService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(MAIN_ACTIVITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("123-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("123-2"),
                mainActivityRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        mainActivityService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MAIN_ACTIVITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("123-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("123-1"),
                mainActivityRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        mainActivityService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MAIN_ACTIVITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("123-2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MainActivityRepository mainActivityRepositorySpy = spy(this.mainActivityRepository);

        MainActivity mainActivity = MainActivity.builder()
                .uri("http://uri.no")
                .code("MAIN_ACTIVITY")
                .label(Map.of("en", "My activity"))
                .build();
        mainActivityRepositorySpy.save(mainActivity);


        long count = mainActivityRepositorySpy.count();
        assertTrue(count > 0);

        when(mainActivityRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        MainActivityService mainActivityService = new MainActivityService(
                new LocalMainActivityHarvester("123"),
                mainActivityRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, mainActivityRepositorySpy.count());
    }
}
