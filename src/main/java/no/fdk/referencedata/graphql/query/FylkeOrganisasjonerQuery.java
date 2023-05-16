package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjon;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class FylkeOrganisasjonerQuery implements GraphQLQueryResolver {

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    public List<FylkeOrganisasjon> getFylkeOrganisasjoner() {
        return StreamSupport.stream(fylkeOrganisasjonRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(FylkeOrganisasjon::getUri))
                .collect(Collectors.toList());
    }

    public FylkeOrganisasjon getFylkeOrganisasjonByFylkesnummer(String fylkesnummer) {
        return fylkeOrganisasjonRepository.findByFylkesnummer(fylkesnummer).orElse(null);
    }
}
