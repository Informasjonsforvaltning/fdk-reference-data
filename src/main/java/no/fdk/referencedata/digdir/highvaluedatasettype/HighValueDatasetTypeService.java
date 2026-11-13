package no.fdk.referencedata.digdir.highvaluedatasettype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.HarvestResult;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HighValueDatasetTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "high-value-dataset-types-source";

    private final HighValueDatasetTypeHarvester highValueDatasetTypeHarvester;

    private final HighValueDatasetTypeRepository highValueDatasetTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public HighValueDatasetTypeService(
            HighValueDatasetTypeHarvester highValueDatasetTypeHarvester,
            HighValueDatasetTypeRepository highValueDatasetTypeRepository,
            ReferenceDataServiceSupport support) {
        this.highValueDatasetTypeHarvester = highValueDatasetTypeHarvester;
        this.highValueDatasetTypeRepository = highValueDatasetTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(highValueDatasetTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public HarvestResult harvestAndSave() {
        return support.harvestAndSave(highValueDatasetTypeHarvester, highValueDatasetTypeRepository, dbSourceID, "high-value-dataset-type");
    }
}
