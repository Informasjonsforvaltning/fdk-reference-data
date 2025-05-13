package no.fdk.referencedata.eu.frequency;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalFrequencyHarvester extends FrequencyHarvester {
    private final String version;
    public static final int FREQUENCIES_SIZE = 38;

    public LocalFrequencyHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("frequencies-sparql-result.ttl");
    }
}
