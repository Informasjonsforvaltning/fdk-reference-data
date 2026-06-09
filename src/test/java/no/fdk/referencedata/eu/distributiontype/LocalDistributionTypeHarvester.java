package no.fdk.referencedata.eu.distributiontype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDistributionTypeHarvester extends DistributionTypeHarvester {
    public static final int DISTRIBUTION_TYPES_SIZE = 4;



    @Override
    public Resource getSource() {
        return new ClassPathResource("distribution-types-sparql-result.ttl");
    }
}
