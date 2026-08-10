package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjon;
import no.fdk.referencedata.ssb.fylkeorganisasjoner.FylkeOrganisasjonRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FylkeOrganisasjonerQuery {

    private final FylkeOrganisasjonRepository fylkeOrganisasjonRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<FylkeOrganisasjon> fylkeOrganisasjoner() {
        return support.allSortedByUri(fylkeOrganisasjonRepository, FylkeOrganisasjon::getUri);
    }

    @QueryMapping
    public FylkeOrganisasjon fylkeOrganisasjonByFylkesnummer(@Argument String fylkesnummer) {
        return support.byCode(fylkeOrganisasjonRepository::findByFylkesnummer, fylkesnummer);
    }
}
