package no.fdk.referencedata.eu.eurovoc;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEuroVocHarvester extends EuroVocHarvester {
    private final String version;

    public LocalEuroVocHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String cellarURI, final String fileName) {
        return new ClassPathResource("eurovoc_in_skos_core_concepts.zip");
    }
}
