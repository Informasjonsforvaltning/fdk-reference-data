package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.continent.Continent;
import no.fdk.referencedata.eu.continent.ContinentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class ContinentQuery {

    @Autowired
    private ContinentRepository continentRepository;

    @QueryMapping
    public List<Continent> continents() {
        return StreamSupport.stream(continentRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Continent::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Continent continentByCode(@Argument String code) {
        return continentRepository.findByCode(code).orElse(null);
    }
}
