package no.fdk.referencedata.eu.frequency;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FrequencyRepository extends CrudRepository<Frequency, String> {
    Optional<Frequency> findByCode(String code);
}
