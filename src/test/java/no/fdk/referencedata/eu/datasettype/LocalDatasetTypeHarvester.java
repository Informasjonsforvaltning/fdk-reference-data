package no.fdk.referencedata.eu.datasettype;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalDatasetTypeHarvester extends DatasetTypeHarvester {
    private final String version;
    public static final int DATASET_TYPES_SIZE = 24;

    public LocalDatasetTypeHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("dataset-types-sparql-result.ttl");
    }
}
