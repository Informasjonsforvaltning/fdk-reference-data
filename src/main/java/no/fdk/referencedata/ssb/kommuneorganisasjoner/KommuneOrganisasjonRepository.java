package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KommuneOrganisasjonRepository extends CrudRepository<KommuneOrganisasjon, String> {
    Optional<KommuneOrganisasjon> findByKommunenummer(String kommunenummer);
}
