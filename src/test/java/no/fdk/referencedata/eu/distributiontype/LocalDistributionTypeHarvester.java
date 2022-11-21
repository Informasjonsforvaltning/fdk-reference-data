package no.fdk.referencedata.eu.distributiontype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDistributionTypeHarvester extends DistributionTypeHarvester {
    private final String version;

    public LocalDistributionTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String sparqlQuery) {
        return new ClassPathResource("distribution-types-sparql-result.ttl");
    }
}
