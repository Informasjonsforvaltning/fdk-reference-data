package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailability;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlannedAvailabilityQuery {

    private final PlannedAvailabilityRepository plannedAvailabilityRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<PlannedAvailability> plannedAvailabilities() {
        return support.allSortedByUri(plannedAvailabilityRepository, PlannedAvailability::getUri);
    }

    @QueryMapping
    public PlannedAvailability plannedAvailabilityByCode(@Argument String code) {
        return support.byCode(plannedAvailabilityRepository::findByCode, code);
    }
}
