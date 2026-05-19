package no.fdk.referencedata.eu.country;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalCountryHarvester extends CountryHarvester {
    private final String version;
    public static final int COUNTRIES_SIZE = 3;

    public LocalCountryHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("country-sparql-result.ttl");
    }
}
