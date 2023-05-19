package no.fdk.referencedata.ssb.kommuneorganisasjoner;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface KommuneOrganisasjonRepository extends CrudRepository<KommuneOrganisasjon, String> {
    Optional<KommuneOrganisasjon> findByKommunenummer(String kommunenummer);
}
