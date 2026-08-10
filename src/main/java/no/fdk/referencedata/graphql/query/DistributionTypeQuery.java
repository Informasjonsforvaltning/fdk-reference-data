package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.distributiontype.DistributionType;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DistributionTypeQuery {

    private final DistributionTypeRepository distributionTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<DistributionType> distributionTypes() {
        return support.allSortedByUri(distributionTypeRepository, DistributionType::getUri);
    }

    @QueryMapping
    public DistributionType distributionTypeByCode(@Argument String code) {
        return support.byCode(distributionTypeRepository::findByCode, code);
    }
}
