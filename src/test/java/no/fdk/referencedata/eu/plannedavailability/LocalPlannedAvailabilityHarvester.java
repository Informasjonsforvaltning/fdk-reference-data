package no.fdk.referencedata.eu.plannedavailability;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalPlannedAvailabilityHarvester extends PlannedAvailabilityHarvester {
    private final String version;
    public static final int PLANNED_AVAILABILITY_SIZE = 4;

    public LocalPlannedAvailabilityHarvester(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Resource getSource() {
        return new ClassPathResource("planned-availability-sparql-result.ttl");
    }
}
