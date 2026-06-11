package no.fdk.referencedata.eu.language;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<Language, String> {
    Optional<Language> findByCode(String code);
    List<Language> findByUriIn(List<String> uris);

    @Query(value = "SELECT * FROM languages WHERE label::text ILIKE '%' || :q || '%'", nativeQuery = true)
    List<Language> findByLabelContaining(@Param("q") String q);
}
