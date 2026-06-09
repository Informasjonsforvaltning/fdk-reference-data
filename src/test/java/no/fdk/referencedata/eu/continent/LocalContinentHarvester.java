package no.fdk.referencedata.eu.continent;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalContinentHarvester extends ContinentHarvester {
    public static final int CONTINENTS_SIZE = 3;



    @Override
    public Resource getSource() {
        return new ClassPathResource("continent-sparql-result.ttl");
    }
}
