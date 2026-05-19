package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.geonames.GeonamesKommune;
import no.fdk.referencedata.geonames.GeonamesKommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class GeonamesKommuneQuery {

    @Autowired
    private GeonamesKommuneRepository geonamesKommuneRepository;

    @QueryMapping
    public List<GeonamesKommune> geonamesKommuner() {
        return StreamSupport.stream(geonamesKommuneRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(GeonamesKommune::getName))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public GeonamesKommune geonamesKommuneByGeonameId(@Argument String geonameId) {
        return geonamesKommuneRepository.findByGeonameId(geonameId).orElse(null);
    }
}
