package no.fdk.referencedata.eu.licence;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLicenceHarvester extends LicenceHarvester {
    public static final int LICENCES_SIZE = 173;
    private final String version;

    public LocalLicenceHarvester(String version) {
        super();
        this.version = version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("licences-sparql-result.ttl");
    }

    @Override
    public String getVersion() {
        return version;
    }
} 
