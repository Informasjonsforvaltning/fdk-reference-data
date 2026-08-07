package no.fdk.referencedata.eu.distributionstatus;

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

import static no.fdk.referencedata.eu.distributionstatus.LocalDistributionStatusHarvester.DISTRIBUTION_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DistributionStatusServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private DistributionStatusRepository distributionStatusRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_distribution_statuses() {
        DistributionStatusService distributionStatusService = new DistributionStatusService(
                new LocalDistributionStatusHarvester(),
                distributionStatusRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        distributionStatusService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        distributionStatusRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(DISTRIBUTION_STATUS_SIZE, counter.get());

        final DistributionStatus first = distributionStatusRepository.findById("http://publications.europa.eu/resource/authority/distribution-status/WITHDRAWN").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/distribution-status/WITHDRAWN", first.getUri());
        assertEquals("WITHDRAWN", first.getCode());
        assertEquals("trukket tilbake", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_if_harvest_rolls_back_transaction_when_save_fails() {
        DistributionStatusRepository distributionStatusRepositorySpy = spy(this.distributionStatusRepository);

        DistributionStatus distributionStatus = DistributionStatus.builder()
                .uri("http://uri.no")
                .code("DISTRIBUTION_STATUS_A")
                .label(Map.of("en", "My distribution status"))
                .build();
        distributionStatusRepositorySpy.save(distributionStatus);

        long count = distributionStatusRepositorySpy.count();
        assertTrue(count > 0);

        when(distributionStatusRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new DistributionStatusService(
                new LocalDistributionStatusHarvester(),
                distributionStatusRepository,
                rdfSourceRepository,
                new ReferenceDataWriter(rdfSourceRepository));

        assertEquals(count, distributionStatusRepositorySpy.count());
    }
}
