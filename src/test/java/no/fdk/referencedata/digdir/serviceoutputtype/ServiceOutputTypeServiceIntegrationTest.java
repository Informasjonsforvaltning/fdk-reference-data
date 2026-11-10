package no.fdk.referencedata.digdir.serviceoutputtype;

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
public class ServiceOutputTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ServiceOutputTypeRepository serviceOutputTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_service_output_types() {
        ServiceOutputTypeService serviceOutputTypeService = new ServiceOutputTypeService(
                LocalHarvesters.serviceOutputType(),
                serviceOutputTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        serviceOutputTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        serviceOutputTypeRepository.findAll().forEach(serviceOutputType -> counter.incrementAndGet());
        assertEquals(8, counter.get());

        final ServiceOutputType first = serviceOutputTypeRepository.findById("https://data.norge.no/vocabulary/service-output-type#financial-benefit").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/service-output-type#financial-benefit", first.getUri());
        assertEquals("financial-benefit", first.getCode());
        assertEquals("financial benefit", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ServiceOutputTypeRepository serviceOutputTypeRepositorySpy = spy(this.serviceOutputTypeRepository);

        ServiceOutputType serviceOutputType = ServiceOutputType.builder()
                .uri("http://uri.no")
                .code("SERVICE_OUTPUT_TYPE")
                .label(Map.of("en", "My service output"))
                .build();
        serviceOutputTypeRepositorySpy.save(serviceOutputType);

        long count = serviceOutputTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(serviceOutputTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new ServiceOutputTypeService(
                LocalHarvesters.serviceOutputType(),
                serviceOutputTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, serviceOutputTypeRepositorySpy.count());
    }
}
