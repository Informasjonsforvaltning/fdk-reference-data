package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface EnhetRepository extends CrudRepository<Enhet, String> {
    Stream<Enhet> findByUriIn(List<String> uris);
    Optional<Enhet> findByCode(String code);
    Stream<Enhet> findByNameContainingIgnoreCase(String query);
}
