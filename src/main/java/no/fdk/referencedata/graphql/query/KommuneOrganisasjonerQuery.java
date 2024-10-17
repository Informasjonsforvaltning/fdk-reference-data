package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjon;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class KommuneOrganisasjonerQuery {

    @Autowired
    private KommuneOrganisasjonRepository kommuneOrganisasjonRepository;

    @QueryMapping
    public List<KommuneOrganisasjon> kommuneOrganisasjoner() {
        return StreamSupport.stream(kommuneOrganisasjonRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(KommuneOrganisasjon::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public KommuneOrganisasjon kommuneOrganisasjonByKommunenummer(@Argument String kommunenummer) {
        return kommuneOrganisasjonRepository.findByKommunenummer(kommunenummer).orElse(null);
    }
}
