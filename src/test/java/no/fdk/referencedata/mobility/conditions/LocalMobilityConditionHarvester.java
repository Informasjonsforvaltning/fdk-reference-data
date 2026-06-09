package no.fdk.referencedata.mobility.conditions;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalMobilityConditionHarvester extends MobilityConditionHarvester {



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("mobility-conditions.ttl");
    }
}
