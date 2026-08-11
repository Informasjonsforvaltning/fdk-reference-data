package no.fdk.referencedata.eu.distributiontype;

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

import static no.fdk.referencedata.LocalHarvestFixtures.DISTRIBUTION_TYPES_SIZE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class DistributionTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_distribution_types() {
        DistributionTypeService accessRightService = new DistributionTypeService(
                LocalHarvesters.distributionType(),
                distributionTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        accessRightService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        distributionTypeRepository.findAll().forEach(accessRight -> counter.incrementAndGet());
        assertEquals(DISTRIBUTION_TYPES_SIZE, counter.get());

        final DistributionType first = distributionTypeRepository.findById("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/distribution-type/DOWNLOADABLE_FILE", first.getUri());
        assertEquals("DOWNLOADABLE_FILE", first.getCode());
        assertEquals("Downloadable file", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        DistributionTypeRepository distrubutionTypeRepositorySpy = spy(this.distributionTypeRepository);

        DistributionType distributionType = DistributionType.builder()
                .uri("http://uri.no")
                .code("DISTRIBUTION_TYPE_A")
                .label(Map.of("en", "My distribution type"))
                .build();
        distrubutionTypeRepositorySpy.save(distributionType);

        long count = distrubutionTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(distrubutionTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new DistributionTypeService(
                LocalHarvesters.distributionType(),
                distributionTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, distrubutionTypeRepositorySpy.count());
    }
}
