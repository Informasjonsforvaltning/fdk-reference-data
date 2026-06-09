package no.fdk.referencedata.eu.plannedavailability;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.eu.plannedavailability.LocalPlannedAvailabilityHarvester.PLANNED_AVAILABILITY_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class PlannedAvailabilityServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private PlannedAvailabilityRepository plannedAvailabilityRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_planned_availabilities() {
        PlannedAvailabilityService plannedAvailabilityService = new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester(),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                new PlannedAvailabilityWriter(plannedAvailabilityRepository, rdfSourceRepository));

        plannedAvailabilityService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        plannedAvailabilityRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(PLANNED_AVAILABILITY_SIZE, counter.get());

        final PlannedAvailability first = plannedAvailabilityRepository.findById("http://publications.europa.eu/resource/authority/planned-availability/EXPERIMENTAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/planned-availability/EXPERIMENTAL", first.getUri());
        assertEquals("EXPERIMENTAL", first.getCode());
        assertEquals("eksperimentell", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
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

        new PlannedAvailabilityService(
                new LocalPlannedAvailabilityHarvester(),
                plannedAvailabilityRepository,
                rdfSourceRepository,
                new PlannedAvailabilityWriter(plannedAvailabilityRepository, rdfSourceRepository));

        assertEquals(count, plannedAvailabilityRepositorySpy.count());
    }
}
