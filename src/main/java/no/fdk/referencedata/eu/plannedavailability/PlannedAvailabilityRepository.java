package no.fdk.referencedata.eu.plannedavailability;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlannedAvailabilityRepository extends CrudRepository<PlannedAvailability, String> {
    Optional<PlannedAvailability> findByCode(String code);
}
