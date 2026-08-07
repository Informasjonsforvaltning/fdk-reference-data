package no.fdk.referencedata.digdir.servicechanneltype;

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
public class ServiceChannelTypeServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ServiceChannelTypeRepository serviceChannelTypeRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_datathemes() {
        ServiceChannelTypeService serviceChannelTypeService = new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester(),
                serviceChannelTypeRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        serviceChannelTypeService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        serviceChannelTypeRepository.findAll().forEach(serviceChannelType -> counter.incrementAndGet());
        assertEquals(11, counter.get());

        final ServiceChannelType first = serviceChannelTypeRepository.findById("https://data.norge.no/vocabulary/service-channel-type#service-bureau").orElseThrow();
        assertEquals("https://data.norge.no/vocabulary/service-channel-type#service-bureau", first.getUri());
        assertEquals("service-bureau", first.getCode());
        assertEquals("service bureau", first.getLabel().get(Language.ENGLISH.code()));
    }

    @Test
    public void test_if_harvest_rollsback_transaction_when_save_fails() {
        ServiceChannelTypeRepository serviceChannelTypeRepositorySpy = spy(this.serviceChannelTypeRepository);

        ServiceChannelType serviceChannelType = ServiceChannelType.builder()
                .uri("http://uri.no")
                .code("SERVICE_CHANNEL_TYPE")
                .label(Map.of("en", "My channel"))
                .build();
        serviceChannelTypeRepositorySpy.save(serviceChannelType);

        long count = serviceChannelTypeRepositorySpy.count();
        assertTrue(count > 0);

        when(serviceChannelTypeRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new ServiceChannelTypeService(
                new LocalServiceChannelTypeHarvester(),
                serviceChannelTypeRepositorySpy,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository));

        assertEquals(count, serviceChannelTypeRepositorySpy.count());
    }
}
