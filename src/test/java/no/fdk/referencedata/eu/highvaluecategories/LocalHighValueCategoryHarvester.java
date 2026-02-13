package no.fdk.referencedata.eu.highvaluecategories;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalHighValueCategoryHarvester extends HighValueCategoriesHarvester {
    private final String version;
    public static final int HIGH_VALUE_CATEGORIES_SIZE = 96;

    public LocalHighValueCategoryHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("high-value-categories.ttl");
    }
}
