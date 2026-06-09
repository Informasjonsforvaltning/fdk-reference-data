package no.fdk.referencedata.eu.frequency;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalFrequencyHarvester extends FrequencyHarvester {
    public static final int FREQUENCIES_SIZE = 38;



    @Override
    public Resource getSource() {
        return new ClassPathResource("frequencies-sparql-result.ttl");
    }
}
