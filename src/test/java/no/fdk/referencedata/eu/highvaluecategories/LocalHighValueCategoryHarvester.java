package no.fdk.referencedata.eu.highvaluecategories;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalHighValueCategoryHarvester extends HighValueCategoriesHarvester {
    public static final int HIGH_VALUE_CATEGORIES_SIZE = 96;

    @Override
    public Resource getSource() {
        return new ClassPathResource("high-value-categories.ttl");
    }
}
