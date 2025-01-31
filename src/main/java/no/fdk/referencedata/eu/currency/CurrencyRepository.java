package no.fdk.referencedata.eu.currency;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CurrencyRepository extends CrudRepository<Currency, String> {
    Optional<Currency> findByCode(String code);
}
