package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.digdir.highvaluedatasettype.HighValueDatasetType;
import no.fdk.referencedata.digdir.highvaluedatasettype.HighValueDatasetTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HighValueDatasetTypeQuery {

    private final HighValueDatasetTypeRepository highValueDatasetTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<HighValueDatasetType> highValueDatasetTypes() {
        return support.allSortedByUri(highValueDatasetTypeRepository, HighValueDatasetType::getUri);
    }

    @QueryMapping
    public HighValueDatasetType highValueDatasetTypeByCode(@Argument String code) {
        return support.byCode(highValueDatasetTypeRepository::findByCode, code);
    }
}
