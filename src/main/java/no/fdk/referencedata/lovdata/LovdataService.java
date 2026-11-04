package no.fdk.referencedata.lovdata;

import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LovdataService implements HarvestableReferenceData {

    private static final String MODULE_ID = "legal-resource";
    private static final String SOURCE_ID = "legal-resources-source";

    private final LovdataHarvester lovdataHarvester;
    private final LegalResourceRepository legalResourceRepository;
    private final ReferenceDataServiceSupport support;

    @Autowired
    public LovdataService(
            LovdataHarvester lovdataHarvester,
            LegalResourceRepository legalResourceRepository,
            ReferenceDataServiceSupport support) {
        this.lovdataHarvester = lovdataHarvester;
        this.legalResourceRepository = legalResourceRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(legalResourceRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(SOURCE_ID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(
                lovdataHarvester::harvest,
                LegalResourceModel::from,
                legalResourceRepository,
                SOURCE_ID,
                MODULE_ID);
    }
}
