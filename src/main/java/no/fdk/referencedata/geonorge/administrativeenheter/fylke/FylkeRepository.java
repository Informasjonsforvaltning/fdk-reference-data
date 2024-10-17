package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface FylkeRepository extends CrudRepository<Fylke, String> {
    Stream<Fylke> findByUriIn(List<String> uris);
    Optional<Fylke> findByFylkesnummer(String type);
    Stream<Fylke> findByFylkesnavnContainingIgnoreCase(String query);
}
