package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.datasettype.DatasetType;
import no.fdk.referencedata.eu.datasettype.DatasetTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DatasetTypeQuery {

    private final DatasetTypeRepository datasetTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<DatasetType> datasetTypes() {
        return support.allSortedByUri(datasetTypeRepository, DatasetType::getUri);
    }

    @QueryMapping
    public DatasetType datasetTypeByCode(@Argument String code) {
        return support.byCode(datasetTypeRepository::findByCode, code);
    }
}
