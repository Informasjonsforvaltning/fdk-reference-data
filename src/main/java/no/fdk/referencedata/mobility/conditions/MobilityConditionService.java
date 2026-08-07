package no.fdk.referencedata.mobility.conditions;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobilityConditionService implements HarvestableReferenceData {
    private final String dbSourceID = "mobility-condition-source";

    private final MobilityConditionHarvester mobilityConditionHarvester;

    private final MobilityConditionRepository mobilityConditionRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public MobilityConditionService(
            MobilityConditionHarvester mobilityConditionHarvester,
            MobilityConditionRepository mobilityConditionRepository,
            ReferenceDataServiceSupport support) {
        this.mobilityConditionHarvester = mobilityConditionHarvester;
        this.mobilityConditionRepository = mobilityConditionRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(mobilityConditionRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(mobilityConditionHarvester, mobilityConditionRepository, dbSourceID, "mobility conditions");
    }
}
