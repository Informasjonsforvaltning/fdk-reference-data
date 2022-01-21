package no.fdk.referencedata.eu.distributiontype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DistributionTypeRepository extends CrudRepository<DistributionType, String> {
    Optional<DistributionType> findByCode(String code);
}
