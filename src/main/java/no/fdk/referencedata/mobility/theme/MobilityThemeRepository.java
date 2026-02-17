package no.fdk.referencedata.mobility.theme;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityThemeRepository extends CrudRepository<MobilityTheme, String> {
    Optional<MobilityTheme> findByCode(String code);
}
