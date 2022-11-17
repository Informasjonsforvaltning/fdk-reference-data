package no.fdk.referencedata.digdir.evidencetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalEvidenceTypeHarvester extends EvidenceTypeHarvester {
    private final String version;

    public LocalEvidenceTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("evidence-type.ttl");
    }
}
