package no.fdk.referencedata.digdir.legalresourcetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLegalResourceTypeHarvester extends LegalResourceTypeHarvester {
    private final String version;
    public static final int LEGAL_RESOURCE_TYPES_SIZE = 2;

    public LocalLegalResourceTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("legal-resource-type.ttl");
    }
}
