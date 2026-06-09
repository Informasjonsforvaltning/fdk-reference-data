package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.mobility.theme.MobilityThemeWriter;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
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

import static no.fdk.referencedata.settings.Settings.MOBILITY_THEME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MobilityThemeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityThemeRepository mobilityThemeRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_themes() {
        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository, harvestSettingsRepository));

        mobilityThemeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        mobilityThemeRepository.findAll().forEach(theme -> counter.incrementAndGet());
        assertEquals(123, counter.get());

        final MobilityTheme first = mobilityThemeRepository.findById("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details").orElseThrow();
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details", first.getUri());
        assertEquals("vehicle-details", first.getCode());
        assertEquals("Vehicle details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_always_persists_and_updates_version() {
        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave();

        HarvestSettings settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Same version
        mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository, harvestSettingsRepository));

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave();

        settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertTrue(settings.getLatestHarvestDate().isAfter(thirdHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MobilityThemeRepository mobilityThemeRepositorySpy = spy(this.mobilityThemeRepository);

        MobilityTheme theme = MobilityTheme.builder()
                .uri("http://uri.no")
                .code("MOBILITY_THEME")
                .label(Map.of("en", "My theme"))
                .build();
        mobilityThemeRepositorySpy.save(theme);


        long count = mobilityThemeRepositorySpy.count();
        assertTrue(count > 0);

        when(mobilityThemeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new MobilityThemeService(
                new LocalMobilityThemeHarvester(),
                mobilityThemeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository,
                new MobilityThemeWriter(mobilityThemeRepository, rdfSourceRepository, harvestSettingsRepository));

        assertEquals(count, mobilityThemeRepositorySpy.count());
    }
}
