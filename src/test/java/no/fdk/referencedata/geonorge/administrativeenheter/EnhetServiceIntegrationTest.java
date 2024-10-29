package no.fdk.referencedata.geonorge.administrativeenheter;

import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.settings.HarvestSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester.ADMINISTRATIVE_ENHETER_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class EnhetServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private EnhetRepository enhetRepository;

    @Autowired
    private HarvestSettingsRepository harvestSettingsRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_administrative_enheter() {
        EnhetService enhetService = new EnhetService(
                new LocalEnhetHarvester(),
                enhetRepository,
                rdfSourceRepository,
                harvestSettingsRepository);

        enhetService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        enhetRepository.findAll().forEach(activity -> counter.incrementAndGet());
        assertEquals(ADMINISTRATIVE_ENHETER_SIZE, counter.get());

        final Enhet sokndal = enhetRepository.findById("https://data.geonorge.no/administrativeEnheter/kommune/id/173093").orElseThrow();
        assertEquals("https://data.geonorge.no/administrativeEnheter/kommune/id/173093", sokndal.getUri());
        assertEquals("173093", sokndal.getCode());
        assertEquals("Sokndal", sokndal.getName());
    }
}
