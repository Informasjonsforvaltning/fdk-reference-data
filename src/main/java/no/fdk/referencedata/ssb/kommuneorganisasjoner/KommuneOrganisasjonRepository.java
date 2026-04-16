package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KommuneOrganisasjonRepository extends JpaRepository<KommuneOrganisasjon, String> {
    Optional<KommuneOrganisasjon> findByKommunenummer(String kommunenummer);
}
