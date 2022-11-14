package no.fdk.referencedata.eu.accessright;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalAccessRightHarvester extends AccessRightHarvester {
    private final String version;

    public LocalAccessRightHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource(final String cellarURI, final String fileName) {
        return new ClassPathResource("access-right-skos-ap-act.rdf");
    }
}
