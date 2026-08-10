package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjon;
import no.fdk.referencedata.ssb.kommuneorganisasjoner.KommuneOrganisasjonRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class KommuneOrganisasjonerQuery {

    private final KommuneOrganisasjonRepository kommuneOrganisasjonRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<KommuneOrganisasjon> kommuneOrganisasjoner() {
        return support.allSortedByUri(kommuneOrganisasjonRepository, KommuneOrganisasjon::getUri);
    }

    @QueryMapping
    public KommuneOrganisasjon kommuneOrganisasjonByKommunenummer(@Argument String kommunenummer) {
        return support.byCode(kommuneOrganisasjonRepository::findByKommunenummer, kommunenummer);
    }
}
