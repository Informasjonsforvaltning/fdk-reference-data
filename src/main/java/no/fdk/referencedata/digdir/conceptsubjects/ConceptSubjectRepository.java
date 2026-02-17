package no.fdk.referencedata.digdir.conceptsubjects;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConceptSubjectRepository extends CrudRepository<ConceptSubject, String> {
    Optional<ConceptSubject> findByCode(String code);
}
