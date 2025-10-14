package no.fdk.referencedata.mobility.theme;

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
                new LocalMobilityThemeHarvester("1.0.1"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityThemeService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        mobilityThemeRepository.findAll().forEach(theme -> counter.incrementAndGet());
        assertEquals(123, counter.get());

        final MobilityTheme first = mobilityThemeRepository.findById("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details").orElseThrow();
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-theme/vehicle-details", first.getUri());
        assertEquals("vehicle-details", first.getCode());
        assertEquals("Vehicle details", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.0.3"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.0.3", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.1.0"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.0.0"),
                mobilityThemeRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        mobilityThemeService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_THEME.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.0", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
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

        MobilityThemeService mobilityThemeService = new MobilityThemeService(
                new LocalMobilityThemeHarvester("1.2.0"),
                mobilityThemeRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, mobilityThemeRepositorySpy.count());
    }
}
