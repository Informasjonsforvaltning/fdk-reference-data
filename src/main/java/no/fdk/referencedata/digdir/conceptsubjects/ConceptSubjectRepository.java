package no.fdk.referencedata.digdir.conceptsubjects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptSubjectRepository extends JpaRepository<ConceptSubject, String> {
    Optional<ConceptSubject> findByCode(String code);
}
