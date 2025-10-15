package no.fdk.referencedata.mobility.datastandard;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityDataStandardHarvester extends MobilityDataStandardHarvester {
    private final String version;

    public LocalMobilityDataStandardHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-data-standards.ttl");
    }
}
