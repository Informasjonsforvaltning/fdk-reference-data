package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface EnhetRepository extends JpaRepository<Enhet, String> {
    Stream<Enhet> findByUriIn(List<String> uris);
    Optional<Enhet> findByCode(String code);
    Stream<Enhet> findByNameContainingIgnoreCase(String query);
}
