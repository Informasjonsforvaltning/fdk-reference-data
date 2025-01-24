package no.fdk.referencedata.eu.distributionstatus;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DistributionStatusRepository extends CrudRepository<DistributionStatus, String> {
    Optional<DistributionStatus> findByCode(String code);
}
