package no.fdk.referencedata.eu.mainactivity;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMainActivityHarvester extends MainActivityHarvester {
    private final String version;

    public LocalMainActivityHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String sparqlQuery) {
        return new ClassPathResource("main-activity-sparql-result.ttl");
    }
}
