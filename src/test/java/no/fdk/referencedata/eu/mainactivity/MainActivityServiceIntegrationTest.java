package no.fdk.referencedata.eu.mainactivity;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

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

import static no.fdk.referencedata.LocalHarvestFixtures.MAIN_ACTIVITIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class MainActivityServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private MainActivityRepository mainActivityRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        MainActivityService mainActivityService = new MainActivityService(
                LocalHarvesters.mainActivity(),
                mainActivityRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        mainActivityService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        mainActivityRepository.findAll().forEach(activity -> counter.incrementAndGet());
        assertEquals(MAIN_ACTIVITIES_SIZE, counter.get());

        final MainActivity first = mainActivityRepository.findById("http://publications.europa.eu/resource/authority/main-activity/hc-am").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/main-activity/hc-am", first.getUri());
        assertEquals("hc-am", first.getCode());
        assertEquals("Housing and community amenities", first.getLabel().get(Language.ENGLISH.code()));
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

        new MainActivityService(
                LocalHarvesters.mainActivity(),
                mainActivityRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, mainActivityRepositorySpy.count());
    }
}
