package no.fdk.referencedata.eu.productstatus;

import no.fdk.referencedata.LocalHarvesters;
import no.fdk.referencedata.i18n.Language;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Comparator;
import java.util.List;

import static no.fdk.referencedata.LocalHarvestFixtures.PRODUCT_STATUS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
public class ProductStatusHarvesterTest {

    @Test
    public void test_fetch_product_statuses() {
        ProductStatusHarvester harvester = LocalHarvesters.productStatus();

        assertNotNull(harvester.getSource());
        assertEquals("product-status-sparql-result.ttl", harvester.getSource().getFilename());

        List<ProductStatus> productStatuses = harvester.harvest().collectList().block();
        assertNotNull(productStatuses);
        assertEquals(PRODUCT_STATUS_SIZE, productStatuses.size());

        productStatuses.sort(Comparator.comparing(ProductStatus::getUri));
        ProductStatus first = productStatuses.get(0);
        assertEquals("http://publications.europa.eu/resource/authority/product-status/DEVELOPMENT", first.getUri());
        assertEquals("DEVELOPMENT", first.getCode());
        assertEquals("in development", first.getLabel().get(Language.ENGLISH.code()));
    }

}
