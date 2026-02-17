package no.fdk.referencedata.eu.conceptstatus;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptStatusRepository extends CrudRepository<ConceptStatus, String> {
    Optional<ConceptStatus> findByCode(String code);
}
