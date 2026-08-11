package no.fdk.referencedata.mobility.datastandard;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MobilityDataStandardService implements HarvestableReferenceData {
    private final String dbSourceID = "mobility-data-standard-source";

    private final MobilityDataStandardHarvester mobilityDataStandardHarvester;

    private final MobilityDataStandardRepository mobilityDataStandardRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public MobilityDataStandardService(
            MobilityDataStandardHarvester mobilityDataStandardHarvester,
            MobilityDataStandardRepository mobilityDataStandardRepository,
            ReferenceDataServiceSupport support) {
        this.mobilityDataStandardHarvester = mobilityDataStandardHarvester;
        this.mobilityDataStandardRepository = mobilityDataStandardRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(mobilityDataStandardRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(mobilityDataStandardHarvester, mobilityDataStandardRepository, dbSourceID, "mobility-data-standard");
    }
}
