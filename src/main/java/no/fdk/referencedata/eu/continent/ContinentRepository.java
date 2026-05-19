package no.fdk.referencedata.eu.continent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContinentRepository extends JpaRepository<Continent, String> {
    Optional<Continent> findByCode(String code);
    List<Continent> findByUriIn(List<String> uris);

    @Query(value = "SELECT * FROM continents WHERE label::text ILIKE '%' || :q || '%'", nativeQuery = true)
    List<Continent> findByLabelContaining(@Param("q") String q);
}
