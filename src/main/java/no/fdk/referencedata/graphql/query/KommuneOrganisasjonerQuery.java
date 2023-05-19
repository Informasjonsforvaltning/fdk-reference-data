package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjon;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class KommuneOrganisasjonerQuery implements GraphQLQueryResolver {

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    public List<KommuneOrganisasjon> getKommuneOrganisasjoner() {
        return StreamSupport.stream(kommuneOrganisasjonRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(KommuneOrganisasjon::getUri))
                .collect(Collectors.toList());
    }

    public KommuneOrganisasjon getKommuneOrganisasjonByKommunenummer(String kommunenummer) {
        return kommuneOrganisasjonRepository.findByKommunenummer(kommunenummer).orElse(null);
    }
}
