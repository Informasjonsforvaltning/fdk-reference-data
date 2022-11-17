package no.fdk.referencedata.digdir.roletype;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleTypeRepository extends CrudRepository<RoleType, String> {
    Optional<RoleType> findByCode(String code);
}
