package no.fdk.referencedata.digdir.relationshipWithSourceType;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RelationshipWithSourceTypeRepository extends CrudRepository<RelationshipWithSourceType, String> {
    Optional<RelationshipWithSourceType> findByCode(String code);
}
