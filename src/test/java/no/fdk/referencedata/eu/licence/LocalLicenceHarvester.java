package no.fdk.referencedata.eu.licence;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLicenceHarvester extends LicenceHarvester {
    public static final int LICENCES_SIZE = 173;

    @Override
    public Resource getSource() {
        return new ClassPathResource("licences-sparql-result.ttl");
    }

} 
