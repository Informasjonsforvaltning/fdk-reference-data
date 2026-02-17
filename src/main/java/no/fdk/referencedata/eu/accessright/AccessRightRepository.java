package no.fdk.referencedata.eu.accessright;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRightRepository extends CrudRepository<AccessRight, String> {
    Optional<AccessRight> findByCode(String code);
}
