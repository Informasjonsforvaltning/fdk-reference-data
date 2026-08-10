package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.licence.Licence;
import no.fdk.referencedata.eu.licence.LicenceRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LicenceQuery {

    private final LicenceRepository licenceRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Licence> licences() {
        return support.allSortedByUri(licenceRepository, Licence::getUri);
    }

    @QueryMapping
    public Licence licenceByCode(@Argument String code) {
        return support.byCode(licenceRepository::findByCode, code);
    }
}
