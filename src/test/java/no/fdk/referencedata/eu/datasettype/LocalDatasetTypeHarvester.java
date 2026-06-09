package no.fdk.referencedata.eu.datasettype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDatasetTypeHarvester extends DatasetTypeHarvester {
    public static final int DATASET_TYPES_SIZE = 24;



    @Override
    public Resource getSource() {
        return new ClassPathResource("dataset-types-sparql-result.ttl");
    }
}
