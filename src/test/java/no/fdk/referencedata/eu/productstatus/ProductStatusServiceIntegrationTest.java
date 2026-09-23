package no.fdk.referencedata.eu.productstatus;

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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static no.fdk.referencedata.LocalHarvestFixtures.PRODUCT_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "scheduling.enabled=false")
@ActiveProfiles("test")
public class ProductStatusServiceIntegrationTest extends AbstractContainerTest {

    @Autowired
    private ProductStatusRepository productStatusRepository;

    private final RDFSourceRepository rdfSourceRepository = mock(RDFSourceRepository.class);

    @Test
    public void test_if_harvest_persists_product_statuses() {
        ProductStatusService productStatusService = new ProductStatusService(
                LocalHarvesters.productStatus(),
                productStatusRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        productStatusService.harvestAndSave();

        final AtomicInteger counter = new AtomicInteger();
        productStatusRepository.findAll().forEach(status -> counter.incrementAndGet());
        assertEquals(PRODUCT_STATUS_SIZE, counter.get());

        final ProductStatus first = productStatusRepository.findById("http://publications.europa.eu/resource/authority/product-status/PRODUCTION").orElseThrow();
        assertEquals("http://publications.europa.eu/resource/authority/product-status/PRODUCTION", first.getUri());
        assertEquals("PRODUCTION", first.getCode());
        assertEquals("i produksjon", first.getLabel().get(Language.NORWEGIAN_BOKMAAL.code()));
    }

    @Test
    public void test_if_harvest_rolls_back_transaction_when_save_fails() {
        ProductStatusRepository productStatusRepositorySpy = spy(this.productStatusRepository);

        ProductStatus productStatus = ProductStatus.builder()
                .uri("http://uri.no")
                .code("PRODUCT_STATUS_A")
                .label(Map.of("en", "My product status"))
                .build();
        productStatusRepositorySpy.save(productStatus);

        long count = productStatusRepositorySpy.count();
        assertTrue(count > 0);

        when(productStatusRepositorySpy.saveAll(anyIterable())).thenThrow(new RuntimeException());

        new ProductStatusService(
                LocalHarvesters.productStatus(),
                productStatusRepository,
                new ReferenceDataServiceSupport(new ReferenceDataWriter(rdfSourceRepository), rdfSourceRepository, new HarvestMetrics(new SimpleMeterRegistry())));

        assertEquals(count, productStatusRepositorySpy.count());
    }
}
