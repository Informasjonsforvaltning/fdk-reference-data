package no.fdk.referencedata.digdir.legalresourcetype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLegalResourceTypeHarvester extends LegalResourceTypeHarvester {
    public static final int LEGAL_RESOURCE_TYPES_SIZE = 2;



    @Override
    public Resource getSource(final String path) {
        return new ClassPathResource("legal-resource-type.ttl");
    }
}
