package no.fdk.referencedata.eu.frequency;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FrequencyRepository extends CrudRepository<Frequency, String> {
    Optional<Frequency> findByCode(String code);
}
