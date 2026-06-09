package no.fdk.referencedata.eu.conceptstatus;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalConceptStatusHarvester extends ConceptStatusHarvester {
    public static final int CONCEPT_STATUSES_SIZE = 12;

    @Override
    public Resource getSource() {
        return new ClassPathResource("concept-status.ttl");
    }
}
