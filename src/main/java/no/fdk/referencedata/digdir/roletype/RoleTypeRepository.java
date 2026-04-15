package no.fdk.referencedata.digdir.roletype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleTypeRepository extends JpaRepository<RoleType, String> {
    Optional<RoleType> findByCode(String code);
}
