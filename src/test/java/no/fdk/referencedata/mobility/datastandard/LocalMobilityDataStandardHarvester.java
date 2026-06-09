package no.fdk.referencedata.mobility.datastandard;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityDataStandardHarvester extends MobilityDataStandardHarvester {



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-data-standards.ttl");
    }
}
