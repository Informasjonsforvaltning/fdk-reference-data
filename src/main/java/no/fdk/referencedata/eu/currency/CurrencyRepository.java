package no.fdk.referencedata.eu.currency;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends CrudRepository<Currency, String> {
    Optional<Currency> findByCode(String code);
}
