package no.fdk.referencedata.eu.plannedavailability;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlannedAvailabilityRepository extends CrudRepository<PlannedAvailability, String> {
    Optional<PlannedAvailability> findByCode(String code);
}
