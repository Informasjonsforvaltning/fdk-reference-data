package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.continent.Continent;
import no.fdk.referencedata.eu.continent.ContinentRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ContinentQuery {

    private final ContinentRepository continentRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Continent> continents() {
        return support.allSortedByUri(continentRepository, Continent::getUri);
    }

    @QueryMapping
    public Continent continentByCode(@Argument String code) {
        return support.byCode(continentRepository::findByCode, code);
    }
}
