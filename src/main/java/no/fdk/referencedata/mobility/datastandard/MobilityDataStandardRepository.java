package no.fdk.referencedata.mobility.datastandard;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MobilityDataStandardRepository extends CrudRepository<MobilityDataStandard, String> {
    Optional<MobilityDataStandard> findByCode(String code);
}
