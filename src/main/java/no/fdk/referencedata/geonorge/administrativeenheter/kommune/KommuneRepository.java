package no.fdk.referencedata.geonorge.administrativeenheter.kommune;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface KommuneRepository extends CrudRepository<Kommune, String> {
    Optional<Kommune> findByKommunenummer(String type);
    Stream<Kommune> findByKommunenavnNorskContainingIgnoreCase(String query);
}
