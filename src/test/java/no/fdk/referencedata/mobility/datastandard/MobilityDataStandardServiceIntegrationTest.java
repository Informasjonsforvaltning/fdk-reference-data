package no.fdk.referencedata.mobility.datastandard;

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

import static no.fdk.referencedata.settings.Settings.MOBILITY_DATA_STANDARD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MobilityDataStandardServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MobilityDataStandardRepository mobilityDataStandardRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_data_standards() {
        MobilityDataStandardService mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.0.1"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        mobilityDataStandardService.harvestAndSave(false);

        final AtomicInteger counter = new AtomicInteger();
        mobilityDataStandardRepository.findAll().forEach(standard -> counter.incrementAndGet());
        assertEquals(15, counter.get());

        final MobilityDataStandard first = mobilityDataStandardRepository.findById("https://w3id.org/mobilitydcat-ap/mobility-data-standard/siri").orElseThrow();
        assertEquals("https://w3id.org/mobilitydcat-ap/mobility-data-standard/siri", first.getUri());
        assertEquals("siri", first.getCode());
        assertEquals("SIRI", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        MobilityDataStandardService mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.1.1"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        mobilityDataStandardService.harvestAndSave(false);

        HarvestSettings settings =
                harvestSettingsRepository.findById(MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.1.2"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        mobilityDataStandardService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.0.0"),
                mobilityDataStandardRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        mobilityDataStandardService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(MOBILITY_DATA_STANDARD.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("1.1.2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        MobilityDataStandardRepository mobilityDataStandardRepositorySpy = spy(this.mobilityDataStandardRepository);

        MobilityDataStandard standard = MobilityDataStandard.builder()
                .uri("http://uri.no")
                .code("MOBILITY_DATA_STANDARD")
                .label(Map.of("en", "My data standard"))
                .build();
        mobilityDataStandardRepositorySpy.save(standard);


        long count = mobilityDataStandardRepositorySpy.count();
        assertTrue(count > 0);

        when(mobilityDataStandardRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        MobilityDataStandardService mobilityDataStandardService = new MobilityDataStandardService(
                new LocalMobilityDataStandardHarvester("1.2.0"),
                mobilityDataStandardRepositorySpy,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, mobilityDataStandardRepositorySpy.count());
    }
}
