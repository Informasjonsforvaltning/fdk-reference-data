package no.fdk.referencedata.eu.conceptstatus;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConceptStatusService implements HarvestableReferenceData {
    private final String dbSourceID = "concept-status-source";

    private final ConceptStatusHarvester conceptStatusHarvester;

    private final ConceptStatusRepository conceptStatusRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public ConceptStatusService(
            ConceptStatusHarvester conceptStatusHarvester,
            ConceptStatusRepository conceptStatusRepository,
            ReferenceDataServiceSupport support) {
        this.conceptStatusHarvester = conceptStatusHarvester;
        this.conceptStatusRepository = conceptStatusRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(conceptStatusRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(conceptStatusHarvester, conceptStatusRepository, dbSourceID, "concept-status");
    }
}
