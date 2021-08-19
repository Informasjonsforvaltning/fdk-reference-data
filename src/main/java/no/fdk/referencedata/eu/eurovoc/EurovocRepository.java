package no.fdk.referencedata.eu.eurovoc;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EurovocRepository extends CrudRepository<Eurovoc, String> {
    Optional<Eurovoc> findByCode(String code);
}
