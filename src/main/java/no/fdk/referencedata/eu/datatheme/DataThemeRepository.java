package no.fdk.referencedata.eu.datatheme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataThemeRepository extends JpaRepository<DataTheme, String> {
    Optional<DataTheme> findByCode(String code);
}
