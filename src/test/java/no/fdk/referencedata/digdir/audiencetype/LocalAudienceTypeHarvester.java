package no.fdk.referencedata.digdir.audiencetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalAudienceTypeHarvester extends AudienceTypeHarvester {
    private final String version;

    public LocalAudienceTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("audience-type.ttl");
    }
}
