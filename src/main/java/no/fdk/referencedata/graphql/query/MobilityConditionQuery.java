package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.mobility.conditions.MobilityCondition;
import no.fdk.referencedata.mobility.conditions.MobilityConditionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MobilityConditionQuery {
    private final MobilityConditionRepository mobilityConditionRepository;

    @Autowired
    public MobilityConditionQuery(MobilityConditionRepository mobilityConditionRepository) {
        this.mobilityConditionRepository = mobilityConditionRepository;
    }

    @QueryMapping
    public List<MobilityCondition> mobilityConditions() {
        return StreamSupport.stream(mobilityConditionRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MobilityCondition::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public MobilityCondition mobilityConditionByCode(@Argument String code) {
        return mobilityConditionRepository.findByCode(code).orElse(null);
    }
}
