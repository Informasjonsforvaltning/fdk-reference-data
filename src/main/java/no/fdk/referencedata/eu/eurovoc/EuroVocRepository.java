package no.fdk.referencedata.eu.eurovoc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EuroVocRepository extends JpaRepository<EuroVoc, String> {
    Optional<EuroVoc> findByCode(String code);
}
