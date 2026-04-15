package no.fdk.referencedata.eu.distributiontype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistributionTypeRepository extends JpaRepository<DistributionType, String> {
    Optional<DistributionType> findByCode(String code);
}
