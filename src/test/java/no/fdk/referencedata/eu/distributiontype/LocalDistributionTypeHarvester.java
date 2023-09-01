package no.fdk.referencedata.eu.distributiontype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDistributionTypeHarvester extends DistributionTypeHarvester {
    private final String version;
    public static final int DISTRIBUTION_TYPES_SIZE = 4;

    public LocalDistributionTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("distribution-types-sparql-result.ttl");
    }
}
