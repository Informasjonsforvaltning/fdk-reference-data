package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatus;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DistributionStatusQuery {

    private final DistributionStatusRepository distributionStatusRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<DistributionStatus> distributionStatuses() {
        return support.allSortedByUri(distributionStatusRepository, DistributionStatus::getUri);
    }

    @QueryMapping
    public DistributionStatus distributionStatusByCode(@Argument String code) {
        return support.byCode(distributionStatusRepository::findByCode, code);
    }
}
