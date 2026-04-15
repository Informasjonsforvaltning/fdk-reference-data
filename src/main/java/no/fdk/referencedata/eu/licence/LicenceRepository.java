package no.fdk.referencedata.eu.licence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenceRepository extends JpaRepository<Licence, String> {
    Optional<Licence> findByCode(String code);
}
