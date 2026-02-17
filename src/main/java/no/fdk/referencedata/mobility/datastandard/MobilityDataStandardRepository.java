package no.fdk.referencedata.mobility.datastandard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityDataStandardRepository extends CrudRepository<MobilityDataStandard, String> {
    Optional<MobilityDataStandard> findByCode(String code);
}
