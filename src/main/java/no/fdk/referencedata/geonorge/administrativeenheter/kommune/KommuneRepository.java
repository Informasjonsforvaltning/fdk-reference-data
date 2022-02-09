package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface KommuneRepository extends CrudRepository<Kommune, String> {
    Optional<Kommune> findByKommunenummer(String type);
}
