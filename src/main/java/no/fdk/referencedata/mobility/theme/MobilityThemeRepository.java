package no.fdk.referencedata.mobility.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityThemeRepository extends JpaRepository<MobilityTheme, String> {
    Optional<MobilityTheme> findByCode(String code);
}
