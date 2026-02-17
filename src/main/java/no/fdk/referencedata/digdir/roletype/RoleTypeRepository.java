package no.fdk.referencedata.digdir.roletype;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleTypeRepository extends CrudRepository<RoleType, String> {
    Optional<RoleType> findByCode(String code);
}
