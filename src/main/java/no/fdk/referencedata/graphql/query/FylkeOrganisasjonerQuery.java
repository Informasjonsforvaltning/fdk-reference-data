package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjon;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class FylkeOrganisasjonerQuery {

    @Autowired
    private FylkeOrganisasjonRepository fylkeOrganisasjonRepository;

    @QueryMapping
    public List<FylkeOrganisasjon> fylkeOrganisasjoner() {
        return StreamSupport.stream(fylkeOrganisasjonRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(FylkeOrganisasjon::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public FylkeOrganisasjon fylkeOrganisasjonByFylkesnummer(@Argument String fylkesnummer) {
        return fylkeOrganisasjonRepository.findByFylkesnummer(fylkesnummer).orElse(null);
    }
}
