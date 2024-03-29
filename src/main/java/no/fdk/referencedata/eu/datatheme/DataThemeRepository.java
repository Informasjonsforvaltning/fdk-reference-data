package no.fdk.referencedata.eu.datatheme;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DataThemeRepository extends CrudRepository<DataTheme, String> {
    Optional<DataTheme> findByCode(String code);
}
