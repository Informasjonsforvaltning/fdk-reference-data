package no.fdk.referencedata.digdir.evidencetype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "evidence-types-source";

    private final EvidenceTypeHarvester evidenceTypeHarvester;

    private final EvidenceTypeRepository evidenceTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public EvidenceTypeService(
            EvidenceTypeHarvester evidenceTypeHarvester,
            EvidenceTypeRepository evidenceTypeRepository,
            ReferenceDataServiceSupport support) {
        this.evidenceTypeHarvester = evidenceTypeHarvester;
        this.evidenceTypeRepository = evidenceTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(evidenceTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(evidenceTypeHarvester, evidenceTypeRepository, dbSourceID, "evidence-types");
    }
}
