package no.fdk.referencedata.eu.plannedavailability;

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

import static no.fdk.referencedata.eu.plannedavailability.LocalPlannedAvailabilityHarvester.PLANNED_AVAILABILITY_SIZE;
import static no.fdk.referencedata.settings.Settings.PLANNED_AVAILABILITY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class PlannedAvailabilityServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private PlannedAvailabilityRepository plannedAvailabilityRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_planned_availabilities() {
        PlannedAvailabilityService plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("20220715-0"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        plannedAvailabilityService.harvestAndSave(true);

        final AtomicInteger counter = new AtomicInteger();
        plannedAvailabilityRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(PLANNED_AVAILABILITY_SIZE, counter.get());

        final PlannedAvailability first = plannedAvailabilityRepository.findById("http://publications.europa.eu/resource/authority/planned-availability/EXPERIMENTAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/EXPERIMENTAL", first.getUri());
        assertEquals("EXPERIMENTAL", first.getCode());
        assertEquals("eksperimentell", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_if_harvest_only_persists_if_newer_version() {
        PlannedAvailabilityService plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("2"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime firstHarvestDateTime = LocalDateTime.now();
        plannedAvailabilityService.harvestAndSave(true);

        HarvestSettings settings =
                harvestSettingsRepository.findById(PLANNED_AVAILABILITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("2", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(firstHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Newer version
        plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("20220715-1"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime secondHarvestDateTime = LocalDateTime.now();
        plannedAvailabilityService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(PLANNED_AVAILABILITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20220715-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(LocalDateTime.now()));

        // Older version
        plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("20210715-0"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        LocalDateTime thirdHarvestDateTime = LocalDateTime.now();
        plannedAvailabilityService.harvestAndSave(false);

        settings =
                harvestSettingsRepository.findById(PLANNED_AVAILABILITY.name()).orElseThrow();
        assertNotNull(settings);
        assertEquals("20220715-1", settings.getLatestVersion());
        assertTrue(settings.getLatestHarvestDate().isAfter(secondHarvestDateTime));
        assertTrue(settings.getLatestHarvestDate().isBefore(thirdHarvestDateTime));
    }

    @Test
    public void test_if_harvest_rolls_back_transaction_when_save_fails() {
        PlannedAvailabilityRepository plannedAvailabilityRepositorySpy = spy(this.plannedAvailabilityRepository);

        PlannedAvailability plannedAvailability = PlannedAvailability.builder()
                .uri("http://uri.no")
                .code("PLANNED_AVAILABILITY_A")
                .label(Map.of("en", "My planned availability"))
                .build();
        plannedAvailabilityRepositorySpy.save(plannedAvailability);


        long count = plannedAvailabilityRepositorySpy.count();
        assertTrue(count > 0);

        when(plannedAvailabilityRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        PlannedAvailabilityService plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester("20200924-2"),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        assertEquals(count, plannedAvailabilityRepositorySpy.count());
    }
}
