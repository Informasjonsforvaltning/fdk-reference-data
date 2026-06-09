package no.fdk.referencedata.digdir.evidencetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEvidenceTypeHarvester extends EvidenceTypeHarvester {

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("evidence-type.ttl");
    }
}
