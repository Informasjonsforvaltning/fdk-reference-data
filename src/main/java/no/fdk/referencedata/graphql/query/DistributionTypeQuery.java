package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.distributiontype.DistributionType;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class DistributionTypeQuery {

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    @QueryMapping
    public List<DistributionType> distributionTypes() {
        return StreamSupport.stream(distributionTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DistributionType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DistributionType distributionTypeByCode(@Argument String code) {
        return distributionTypeRepository.findByCode(code).orElse(null);
    }
}
