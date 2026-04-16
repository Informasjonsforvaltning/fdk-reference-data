package no.fdk.referencedata.eu.distributionstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributionStatusRepository extends JpaRepository<DistributionStatus, String> {
    Optional<DistributionStatus> findByCode(String code);
}
