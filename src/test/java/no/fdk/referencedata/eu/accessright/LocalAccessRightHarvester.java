package no.fdk.referencedata.eu.accessright;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalAccessRightHarvester extends AccessRightHarvester {
    public static final int ACCESS_RIGHTS_SIZE = 6;



    @Override
    public Resource getSource() {
        return new ClassPathResource("access-right-sparql-result.ttl");
    }
}
