package no.fdk.referencedata.mobility.conditions;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityConditionRepository extends CrudRepository<MobilityCondition, String> {
    Optional<MobilityCondition> findByCode(String code);
}
