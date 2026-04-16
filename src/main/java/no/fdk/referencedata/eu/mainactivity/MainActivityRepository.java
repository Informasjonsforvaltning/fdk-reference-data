package no.fdk.referencedata.eu.mainactivity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MainActivityRepository extends JpaRepository<MainActivity, String> {
    Optional<MainActivity> findByCode(String code);
}
