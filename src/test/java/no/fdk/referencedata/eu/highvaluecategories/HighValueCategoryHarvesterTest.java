package no.fdk.referencedata.eu.highvaluecategories;

import org.junit.jupiter.api.Test;

import static no.fdk.referencedata.eu.highvaluecategories.LocalHighValueCategoryHarvester.HIGH_VALUE_CATEGORIES_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HighValueCategoryHarvesterTest {

    @Test
    public void test_fetch_high_value_categories() {
        HighValueCategoriesHarvester harvester = new LocalHighValueCategoryHarvester("1");

        assertNotNull(harvester.getSource());
        assertEquals(HIGH_VALUE_CATEGORIES_SIZE, harvester.harvest().collectList().block().size());
    }

}
