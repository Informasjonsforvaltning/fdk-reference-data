package no.fdk.referencedata.eu.plannedavailability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlannedAvailabilityRepository extends JpaRepository<PlannedAvailability, String> {
    Optional<PlannedAvailability> findByCode(String code);
}
