package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.mobility.conditions.MobilityCondition;
import no.fdk.referencedata.mobility.conditions.MobilityConditionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MobilityConditionQuery {

    private final MobilityConditionRepository mobilityConditionRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<MobilityCondition> mobilityConditions() {
        return support.allSortedByUri(mobilityConditionRepository, MobilityCondition::getUri);
    }

    @QueryMapping
    public MobilityCondition mobilityConditionByCode(@Argument String code) {
        return support.byCode(mobilityConditionRepository::findByCode, code);
    }
}
