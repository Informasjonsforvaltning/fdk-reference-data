package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import no.fdk.referencedata.eu.distributiontype.DistributionType;
import no.fdk.referencedata.eu.distributiontype.DistributionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DistributionTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private DistributionTypeRepository distributionTypeRepository;

    public List<DistributionType> getDistributionTypes() {
        return StreamSupport.stream(distributionTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DistributionType::getUri))
                .collect(Collectors.toList());
    }

    public DistributionType getDistributionTypeByCode(String code) {
        return distributionTypeRepository.findByCode(code).orElse(null);
    }
}