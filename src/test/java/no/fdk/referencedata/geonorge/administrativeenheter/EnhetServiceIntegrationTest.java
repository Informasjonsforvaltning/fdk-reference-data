package no.fdk.referencedata.geonorge.administrativeenheter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.geonorge.administrativeenheter.LocalEnhetHarvester;
import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.geonorge.administrativeenheter.EnhetWriter;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
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
    private EnhetVariantRepository enhetVariantRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_administrative_enheter() {
        EnhetService enhetService = new EnhetService(
                new LocalEnhetHarvester(),
                enhetRepository,
                enhetVariantRepository,
                new EnhetWriter(enhetRepository, enhetVariantRepository, rdfSourceRepository),
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())), new HarvestMetrics(new SimpleMeterRegistry()));

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
