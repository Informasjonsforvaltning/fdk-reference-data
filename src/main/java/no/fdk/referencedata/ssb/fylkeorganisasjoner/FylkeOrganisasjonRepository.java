package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FylkeOrganisasjonRepository extends CrudRepository<FylkeOrganisasjon, String> {
    Optional<FylkeOrganisasjon> findByFylkesnummer(String fylkesnummer);
}
