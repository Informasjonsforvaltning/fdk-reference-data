package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipWithSourceTypeRepository extends CrudRepository<RelationshipWithSourceType, String> {
    Optional<RelationshipWithSourceType> findByCode(String code);
}
