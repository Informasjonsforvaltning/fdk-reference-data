package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.datasettype.DatasetType;
import no.fdk.referencedata.eu.datasettype.DatasetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DatasetTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    public List<DatasetType> getDatasetTypes() {
        return StreamSupport.stream(datasetTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DatasetType::getUri))
                .collect(Collectors.toList());
    }

    public DatasetType getDatasetTypeByCode(String code) {
        return datasetTypeRepository.findByCode(code).orElse(null);
    }
}
