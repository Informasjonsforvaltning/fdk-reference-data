package no.fdk.referencedata.eu.frequency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FrequencyService implements HarvestableReferenceData {
    private final String dbSourceID = "frequencies-source";

    private final FrequencyHarvester frequencyHarvester;
    private final FrequencyRepository frequencyRepository;
    private final ReferenceDataServiceSupport support;

    @Autowired
    public FrequencyService(
            FrequencyHarvester frequencyHarvester,
            FrequencyRepository frequencyRepository,
            ReferenceDataServiceSupport support) {
        this.frequencyHarvester = frequencyHarvester;
        this.frequencyRepository = frequencyRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(frequencyRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(frequencyHarvester, frequencyRepository, dbSourceID, "frequencies");
    }
}
