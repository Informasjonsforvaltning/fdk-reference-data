package no.fdk.referencedata.eu.accessright;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.core.HarvestMetrics;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import no.fdk.referencedata.core.ReferenceDataWriter;

import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.container.AbstractContainerTest;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.ACCESS_RIGHTS_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class AccessRightServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private AccessRightRepository accessRightRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        AccessRightService accessRightService = new AccessRightService(
                LocalHarvesters.accessRight(),
                accessRightRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        accessRightService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        accessRightRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(ACCESS_RIGHTS_SIZE, counter.get());

        final AccessRight first = accessRightRepository.findById("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/access-right/CONFIDENTIAL", first.getUri());
        assertEquals("CONFIDENTIAL", first.getCode());
        assertEquals("confidential", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        AccessRightRepository accessRightRepositorySpy = spy(this.accessRightRepository);

        AccessRight accessRight = AccessRight.builder()
                .uri("http://uri.no")
                .code("ACCESS_RIGHT")
                .label(Map.of("en", "My right"))
                .build();
        accessRightRepositorySpy.save(accessRight);

        long count = accessRightRepositorySpy.count();
        assertTrue(count > 0);

        when(accessRightRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new AccessRightService(
                LocalHarvesters.accessRight(),
                accessRightRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, accessRightRepositorySpy.count());
    }
}
