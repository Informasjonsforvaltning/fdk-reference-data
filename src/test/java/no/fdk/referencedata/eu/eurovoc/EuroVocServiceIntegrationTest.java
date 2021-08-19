package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mongo.AbstractMongoDbContainerTest;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.settings.Settings.EURO_VOC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = { "scheduling.enabled=false" })
public class EuroVocServiceIntegrationTest extends AbstractMongoDbContainerTest {

    @Autowired
    private EuroVocRepository euroVocRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_eurovoc() {
        EuroVocService euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200923-0"),
                euroVocRepository,
                harvestSettingsRepository);

        euroVocService.harvestAndSaveEuroVoc();

        final AtomicInteger counter = new AtomicInteger();
        euroVocRepository.findAll().forEach(fileType -> counter.incrementAndGet());
        assertEquals(7322, counter.get());

        final EuroVoc euroVoc5548 = euroVocRepository.findById("http://eurovoc.europa.eu/5548").orElseThrow();
        assertEquals("http://eurovoc.europa.eu/5548", euroVoc5548.getUri());
        assertEquals("5548", euroVoc5548.getCode());
        assertEquals("interinstitutional cooperation (EU)", euroVoc5548.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        EuroVocService euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200923-1"),
                euroVocRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSaveEuroVoc();

        HarvestSettings settings =
                harvestSettingsRepository.findById(EURO_VOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200923-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200924-0"),
                euroVocRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSaveEuroVoc();

        settings =
                harvestSettingsRepository.findById(EURO_VOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200924-0"),
                euroVocRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSaveEuroVoc();

        settings =
                harvestSettingsRepository.findById(EURO_VOC.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20200924-0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        EuroVocService EuroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200924-2"),
                EuroVocRepositorySpy,
                harvestSettingsRepository);

        assertEquals(count, EuroVocRepositorySpy.count());
    }
}
