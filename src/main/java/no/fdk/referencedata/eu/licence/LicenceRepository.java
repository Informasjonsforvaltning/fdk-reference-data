package no.fdk.referencedata.eu.licence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LicenceRepository extends CrudRepository<Licence, String> {
    Optional<Licence> findByCode(String code);
}
