package no.fdk.referencedata.eu.eurovoc;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettings;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.eurovoc.LocalEuroVocHarvester.EUROVOCS_SIZE;
import static no.fdk.referencedata.settings.Settings.EURO_VOC;
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

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    @Test
    public void test_if_harvest_persists_eurovoc() {
        EuroVocService euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200923-0"),
                euroVocRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        euroVocService.harvestAndSave(false);

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
    public void test_if_harvest_only_persists_if_newer_version() {
        EuroVocService euroVocService = new EuroVocService(
                new LocalEuroVocHarvester("20200923-1"),
                euroVocRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSave(false);

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
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSave(false);

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
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        euroVocService.harvestAndSave(false);

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
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, EuroVocRepositorySpy.count());
    }
}
