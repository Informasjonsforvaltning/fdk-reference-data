package no.fdk.referencedata.eu.eurovoc;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EuroVocRepository extends CrudRepository<EuroVoc, String> {
    Optional<EuroVoc> findByCode(String code);
}
