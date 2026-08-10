package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.LocalHarvesters;
import org.junit.jupiter.api.Test;

import static no.fdk.referencedata.LocalHarvestFixtures.HIGH_VALUE_CATEGORIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HighValueCategoryHarvesterTest {

    @Test
    public void test_fetch_high_value_categories() {
        HighValueCategoriesHarvester harvester = LocalHarvesters.highValueCategory();

        assertNotNull(harvester.getSource());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, harvester.harvest().collectList().block().size());
    }

}
