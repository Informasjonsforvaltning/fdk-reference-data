package no.fdk.referencedata.eu.conceptstatus;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConceptStatusRepository extends CrudRepository<ConceptStatus, String> {
    Optional<ConceptStatus> findByCode(String code);
}
