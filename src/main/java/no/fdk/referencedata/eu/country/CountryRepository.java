package no.fdk.referencedata.eu.country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, String> {
    Optional<Country> findByCode(String code);
    List<Country> findByUriIn(List<String> uris);

    @Query(value = "SELECT * FROM countries WHERE label::text ILIKE '%' || :q || '%'", nativeQuery = true)
    List<Country> findByLabelContaining(@Param("q") String q);
}
