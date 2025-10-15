package no.fdk.referencedata.mobility.conditions;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MobilityConditionRepository extends CrudRepository<MobilityCondition, String> {
    Optional<MobilityCondition> findByCode(String code);
}
