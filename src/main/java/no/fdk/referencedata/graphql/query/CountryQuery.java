package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.country.Country;
import no.fdk.referencedata.eu.country.CountryRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CountryQuery {

    private final CountryRepository countryRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Country> countries() {
        return support.allSortedByUri(countryRepository, Country::getUri);
    }

    @QueryMapping
    public Country countryByCode(@Argument String code) {
        return support.byCode(countryRepository::findByCode, code);
    }
}
