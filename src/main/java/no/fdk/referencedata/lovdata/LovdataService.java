package no.fdk.referencedata.lovdata;

import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LovdataService implements HarvestableReferenceData {

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

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSaveWithoutRdf(
                lovdataHarvester::harvest,
                legalResourceRepository,
                "legal-resource");
    }
}
