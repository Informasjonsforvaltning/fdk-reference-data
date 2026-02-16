package no.fdk.referencedata.digdir.legalresourcetype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LegalResourceTypeRepository extends CrudRepository<LegalResourceType, String> {
    Optional<LegalResourceType> findByCode(String code);
}
