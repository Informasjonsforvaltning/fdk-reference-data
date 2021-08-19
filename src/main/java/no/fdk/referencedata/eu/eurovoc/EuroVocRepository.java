package no.fdk.referencedata.eu.eurovoc;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EuroVocRepository extends CrudRepository<EuroVoc, String> {
    Optional<EuroVoc> findByCode(String code);
}
