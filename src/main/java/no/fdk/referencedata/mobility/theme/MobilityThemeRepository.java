package no.fdk.referencedata.mobility.theme;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MobilityThemeRepository extends CrudRepository<MobilityTheme, String> {
    Optional<MobilityTheme> findByCode(String code);
}
