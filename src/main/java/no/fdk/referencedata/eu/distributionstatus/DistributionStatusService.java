package no.fdk.referencedata.eu.distributionstatus;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributionStatusService implements HarvestableReferenceData {
    private final String dbSourceID = "distribution-statuses-source";

    private final DistributionStatusHarvester distributionStatusHarvester;

    private final DistributionStatusRepository distributionStatusRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public DistributionStatusService(
            DistributionStatusHarvester distributionStatusHarvester,
            DistributionStatusRepository distributionStatusRepository,
            ReferenceDataServiceSupport support) {
        this.distributionStatusHarvester = distributionStatusHarvester;
        this.distributionStatusRepository = distributionStatusRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(distributionStatusRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(distributionStatusHarvester, distributionStatusRepository, dbSourceID, "distribution-status");
    }
}
