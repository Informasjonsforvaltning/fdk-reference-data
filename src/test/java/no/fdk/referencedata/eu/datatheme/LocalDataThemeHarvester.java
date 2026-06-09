package no.fdk.referencedata.eu.datatheme;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDataThemeHarvester extends DataThemeHarvester {
    public static final int DATA_THEMES_SIZE = 13;

    @Override
    public Resource getSource() {
        return new ClassPathResource("data-theme-sparql-result.ttl");
    }
}
