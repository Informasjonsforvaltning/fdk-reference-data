package no.fdk.referencedata.geonames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeonamesFylkeRepository extends JpaRepository<GeonamesFylke, String> {
    Optional<GeonamesFylke> findByGeonameId(String geonameId);
    List<GeonamesFylke> findByNameContainingIgnoreCase(String name);
    List<GeonamesFylke> findByUriIn(List<String> uris);
}
