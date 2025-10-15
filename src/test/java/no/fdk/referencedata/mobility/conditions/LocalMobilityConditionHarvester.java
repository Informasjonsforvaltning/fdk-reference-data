package no.fdk.referencedata.mobility.conditions;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityConditionHarvester extends MobilityConditionHarvester {
    private final String version;

    public LocalMobilityConditionHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-conditions.ttl");
    }
}
