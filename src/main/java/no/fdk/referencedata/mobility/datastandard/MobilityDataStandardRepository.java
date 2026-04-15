package no.fdk.referencedata.mobility.datastandard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityDataStandardRepository extends JpaRepository<MobilityDataStandard, String> {
    Optional<MobilityDataStandard> findByCode(String code);
}
