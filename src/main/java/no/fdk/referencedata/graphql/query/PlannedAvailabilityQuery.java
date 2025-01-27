package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.plannedavailability.PlannedAvailability;
import no.fdk.referencedata.eu.plannedavailability.PlannedAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class PlannedAvailabilityQuery {

    @Autowired
    private PlannedAvailabilityRepository plannedAvailabilityRepository;

    @QueryMapping
    public List<PlannedAvailability> plannedAvailabilities() {
        return StreamSupport.stream(plannedAvailabilityRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(PlannedAvailability::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public PlannedAvailability plannedAvailabilityByCode(@Argument String code) {
        return plannedAvailabilityRepository.findByCode(code).orElse(null);
    }
}
