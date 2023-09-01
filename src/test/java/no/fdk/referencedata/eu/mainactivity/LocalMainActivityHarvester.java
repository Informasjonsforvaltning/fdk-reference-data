package no.fdk.referencedata.eu.mainactivity;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMainActivityHarvester extends MainActivityHarvester {
    private final String version;
    public static final int MAIN_ACTIVITIES_SIZE = 20;

    public LocalMainActivityHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("main-activity-sparql-result.ttl");
    }
}
