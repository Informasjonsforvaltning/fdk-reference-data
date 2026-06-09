package no.fdk.referencedata.eu.mainactivity;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMainActivityHarvester extends MainActivityHarvester {
    public static final int MAIN_ACTIVITIES_SIZE = 20;

    @Override
    public Resource getSource() {
        return new ClassPathResource("main-activity-sparql-result.ttl");
    }
}
