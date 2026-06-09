package no.fdk.referencedata.digdir.servicechanneltype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalServiceChannelTypeHarvester extends ServiceChannelTypeHarvester {



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("service-channel-type.ttl");
    }
}
