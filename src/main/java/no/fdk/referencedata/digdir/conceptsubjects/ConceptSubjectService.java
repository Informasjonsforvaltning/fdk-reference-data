package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConceptSubjectService implements HarvestableReferenceData {
    private final String dbSourceID = "concept-subjects-source";

    private final ConceptSubjectHarvester conceptSubjectHarvester;

    private final ConceptSubjectRepository conceptSubjectRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ConceptSubjectService(
            ConceptSubjectHarvester conceptSubjectHarvester,
            ConceptSubjectRepository conceptSubjectRepository,
            ReferenceDataServiceSupport support) {
        this.conceptSubjectHarvester = conceptSubjectHarvester;
        this.conceptSubjectRepository = conceptSubjectRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(conceptSubjectRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(conceptSubjectHarvester, conceptSubjectRepository, dbSourceID, "concept subjects");
    }
}
