package no.fdk.referencedata.mobility.conditions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityConditionRepository extends JpaRepository<MobilityCondition, String> {
    Optional<MobilityCondition> findByCode(String code);
}
