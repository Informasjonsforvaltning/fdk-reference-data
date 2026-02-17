package no.fdk.referencedata.eu.distributiontype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributionTypeRepository extends CrudRepository<DistributionType, String> {
    Optional<DistributionType> findByCode(String code);
}
