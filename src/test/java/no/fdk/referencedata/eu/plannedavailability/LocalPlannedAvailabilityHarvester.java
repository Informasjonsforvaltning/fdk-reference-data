package no.fdk.referencedata.eu.plannedavailability;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalPlannedAvailabilityHarvester extends PlannedAvailabilityHarvester {
    public static final int PLANNED_AVAILABILITY_SIZE = 4;



    @Override
    public Resource getSource() {
        return new ClassPathResource("planned-availability-sparql-result.ttl");
    }
}
