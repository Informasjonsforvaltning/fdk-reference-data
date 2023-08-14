package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.ApplicationSettings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalConceptSubjectHarvester extends ConceptSubjectHarvester {
    private final String version = "0";

    public LocalConceptSubjectHarvester(ApplicationSettings appSettings) {
        super(appSettings);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("concept-subjects.ttl");
    }
}
