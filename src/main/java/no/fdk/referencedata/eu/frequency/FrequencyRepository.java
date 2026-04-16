package no.fdk.referencedata.eu.frequency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FrequencyRepository extends JpaRepository<Frequency, String> {
    Optional<Frequency> findByCode(String code);
}
