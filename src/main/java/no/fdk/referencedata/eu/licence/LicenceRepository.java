package no.fdk.referencedata.eu.licence;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LicenceRepository extends CrudRepository<Licence, String> {
    Optional<Licence> findByCode(String code);
}
