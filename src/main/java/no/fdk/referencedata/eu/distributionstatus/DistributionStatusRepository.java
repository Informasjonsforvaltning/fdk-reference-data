package no.fdk.referencedata.eu.distributionstatus;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributionStatusRepository extends CrudRepository<DistributionStatus, String> {
    Optional<DistributionStatus> findByCode(String code);
}
