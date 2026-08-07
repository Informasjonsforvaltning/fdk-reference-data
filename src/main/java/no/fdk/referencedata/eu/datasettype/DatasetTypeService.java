package no.fdk.referencedata.eu.datasettype;

import no.fdk.referencedata.core.HarvestableReferenceData;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;

import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetTypeService implements HarvestableReferenceData {
    private final String dbSourceID = "dataset-types-source";

    private final DatasetTypeHarvester datasetTypeHarvester;

    private final DatasetTypeRepository datasetTypeRepository;

    private final ReferenceDataServiceSupport support;

    @Autowired
    public DatasetTypeService(
            DatasetTypeHarvester datasetTypeHarvester,
            DatasetTypeRepository datasetTypeRepository,
            ReferenceDataServiceSupport support) {
        this.datasetTypeHarvester = datasetTypeHarvester;
        this.datasetTypeRepository = datasetTypeRepository;
        this.support = support;
    }

    @Override
    public boolean firstTime() {
        return support.firstTime(datasetTypeRepository);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(dbSourceID, rdfFormat);
    }

    @Override
    public void harvestAndSave() {
        support.harvestAndSave(datasetTypeHarvester, datasetTypeRepository, dbSourceID, "dataset-types");
    }
}
