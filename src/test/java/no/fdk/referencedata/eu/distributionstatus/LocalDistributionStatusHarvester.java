package no.fdk.referencedata.eu.distributionstatus;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDistributionStatusHarvester extends DistributionStatusHarvester {
    private final String version;
    public static final int DISTRIBUTION_STATUS_SIZE = 4;

    public LocalDistributionStatusHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("distribution-status-sparql-result.ttl");
    }
}
