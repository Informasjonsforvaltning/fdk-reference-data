package no.fdk.referencedata.digdir.relationshipwithsourcetype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipWithSourceTypeRepository extends JpaRepository<RelationshipWithSourceType, String> {
    Optional<RelationshipWithSourceType> findByCode(String code);
}
