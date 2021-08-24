package no.fdk.referencedata.eu.accessright;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccessRightRepository extends CrudRepository<AccessRight, String> {
    Optional<AccessRight> findByCode(String code);
}
