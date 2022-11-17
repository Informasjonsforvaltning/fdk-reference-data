package no.fdk.referencedata.digdir.servicechanneltype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalServiceChannelTypeHarvester extends ServiceChannelTypeHarvester {
    private final String version;

    public LocalServiceChannelTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("service-channel-type.ttl");
    }
}
