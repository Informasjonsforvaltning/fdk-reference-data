package no.fdk.referencedata.eu.distributiontype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributionTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "distribution-types-source";

    private final DistributionTypeHarvester distributionTypeHarvester;

    private final DistributionTypeRepository distributionTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public DistributionTypeService(
            DistributionTypeHarvester distributionTypeHarvester,
            DistributionTypeRepository distributionTypeRepository,
            ReferenceDataServiceSupport support) {
        this.distributionTypeHarvester = distributionTypeHarvester;
        this.distributionTypeRepository = distributionTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(distributionTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(distributionTypeHarvester, distributionTypeRepository, dbSourceID, "distribution-type");
    }
}
