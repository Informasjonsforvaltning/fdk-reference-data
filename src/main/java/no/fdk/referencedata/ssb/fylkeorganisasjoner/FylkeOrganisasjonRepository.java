package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FylkeOrganisasjonRepository extends JpaRepository<FylkeOrganisasjon, String> {
    Optional<FylkeOrganisasjon> findByFylkesnummer(String fylkesnummer);
}
