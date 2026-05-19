package no.fdk.referencedata.eu.continent;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalContinentHarvester extends ContinentHarvester {
    private final String version;
    public static final int CONTINENTS_SIZE = 3;

    public LocalContinentHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("continent-sparql-result.ttl");
    }
}
