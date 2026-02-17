package no.fdk.referencedata.eu.datatheme;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataThemeRepository extends CrudRepository<DataTheme, String> {
    Optional<DataTheme> findByCode(String code);
}
