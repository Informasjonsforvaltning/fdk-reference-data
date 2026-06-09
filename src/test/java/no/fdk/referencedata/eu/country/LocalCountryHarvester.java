package no.fdk.referencedata.eu.country;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalCountryHarvester extends CountryHarvester {
    public static final int COUNTRIES_SIZE = 3;

    @Override
    public Resource getSource() {
        return new ClassPathResource("country-sparql-result.ttl");
    }
}
