package no.fdk.referencedata.eu.mainactivity;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MainActivityRepository extends CrudRepository<MainActivity, String> {
    Optional<MainActivity> findByCode(String code);
}
