package no.fdk.referencedata.digdir.conceptsubjects;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConceptSubjectRepository extends CrudRepository<ConceptSubject, String> {
    Optional<ConceptSubject> findByCode(String code);
}
