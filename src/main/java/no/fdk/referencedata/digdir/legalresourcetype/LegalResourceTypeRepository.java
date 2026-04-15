package no.fdk.referencedata.digdir.legalresourcetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegalResourceTypeRepository extends JpaRepository<LegalResourceType, String> {
    Optional<LegalResourceType> findByCode(String code);
}
