package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.datasettype.DatasetType;
import no.fdk.referencedata.eu.datasettype.DatasetTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class DatasetTypeQuery {

    @Autowired
    private DatasetTypeRepository datasetTypeRepository;

    @QueryMapping
    public List<DatasetType> datasetTypes() {
        return StreamSupport.stream(datasetTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DatasetType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DatasetType datasetTypeByCode(@Argument String code) {
        return datasetTypeRepository.findByCode(code).orElse(null);
    }
}
