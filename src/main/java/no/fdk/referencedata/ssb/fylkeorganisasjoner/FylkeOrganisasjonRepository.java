package no.fdk.referencedata.ssb.fylkeorganisasjoner;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FylkeOrganisasjonRepository extends CrudRepository<FylkeOrganisasjon, String> {
    Optional<FylkeOrganisasjon> findByFylkesnummer(String fylkesnummer);
}
