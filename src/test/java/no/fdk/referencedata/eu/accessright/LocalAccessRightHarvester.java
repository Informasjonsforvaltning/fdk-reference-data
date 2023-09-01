package no.fdk.referencedata.eu.accessright;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalAccessRightHarvester extends AccessRightHarvester {
    private final String version;
    public static final int ACCESS_RIGHTS_SIZE = 6;

    public LocalAccessRightHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("access-right-sparql-result.ttl");
    }
}
