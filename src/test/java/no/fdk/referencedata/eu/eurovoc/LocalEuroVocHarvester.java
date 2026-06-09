package no.fdk.referencedata.eu.eurovoc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEuroVocHarvester extends EuroVocHarvester {

    public static final int EUROVOCS_SIZE = 7403;

    @Override
    public Resource getSource() {
        return new ClassPathResource("eurovoc-sparql-result.ttl");
    }
}
