package no.fdk.referencedata.eu.accessright;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRightRepository extends JpaRepository<AccessRight, String> {
    Optional<AccessRight> findByCode(String code);
}
