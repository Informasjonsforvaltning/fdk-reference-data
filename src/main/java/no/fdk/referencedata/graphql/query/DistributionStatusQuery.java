package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.distributionstatus.DistributionStatus;
import no.fdk.referencedata.eu.distributionstatus.DistributionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class DistributionStatusQuery {

    @Autowired
    private DistributionStatusRepository distributionStatusRepository;

    @QueryMapping
    public List<DistributionStatus> distributionStatuses() {
        return StreamSupport.stream(distributionStatusRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DistributionStatus::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DistributionStatus distributionStatusByCode(@Argument String code) {
        return distributionStatusRepository.findByCode(code).orElse(null);
    }
}
