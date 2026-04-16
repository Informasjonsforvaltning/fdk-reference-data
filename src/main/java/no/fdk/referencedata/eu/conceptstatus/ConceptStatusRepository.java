package no.fdk.referencedata.eu.conceptstatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptStatusRepository extends JpaRepository<ConceptStatus, String> {
    Optional<ConceptStatus> findByCode(String code);
}
