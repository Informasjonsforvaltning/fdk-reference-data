package no.fdk.referencedata.geonames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeonamesKommuneRepository extends JpaRepository<GeonamesKommune, String> {
    Optional<GeonamesKommune> findByGeonameId(String geonameId);
    List<GeonamesKommune> findByFylkeGeonameId(String fylkeGeonameId);
    List<GeonamesKommune> findByNameContainingIgnoreCase(String name);
    List<GeonamesKommune> findByUriIn(List<String> uris);
}
