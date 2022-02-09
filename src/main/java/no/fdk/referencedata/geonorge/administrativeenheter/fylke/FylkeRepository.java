package no.fdk.referencedata.geonorge.administrativeenheter.fylke;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FylkeRepository extends CrudRepository<Fylke, String> {
    Optional<Fylke> findByFylkesnummer(String type);
}
