package no.fdk.referencedata.eu.conceptstatus;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalConceptStatusHarvester extends ConceptStatusHarvester {
    private final String version;
    public static final int CONCEPT_STATUSES_SIZE = 12;

    public LocalConceptStatusHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("concept-status.ttl");
    }
}
