package no.fdk.referencedata.eu.eurovoc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEurovocHarvester extends EurovocHarvester {
    private final String version;

    public LocalEurovocHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("eurovoc_in_skos_core_concepts.zip");
    }
}
