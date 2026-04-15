package no.fdk.referencedata.geonorge.administrativeenheter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnhetRepository extends JpaRepository<Enhet, String> {
    List<Enhet> findByUriIn(List<String> uris);
    Optional<Enhet> findByCode(String code);
    List<Enhet> findByNameContainingIgnoreCase(String query);
}
