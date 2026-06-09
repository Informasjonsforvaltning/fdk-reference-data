package no.fdk.referencedata.digdir.audiencetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalAudienceTypeHarvester extends AudienceTypeHarvester {



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("audience-type.ttl");
    }
}
