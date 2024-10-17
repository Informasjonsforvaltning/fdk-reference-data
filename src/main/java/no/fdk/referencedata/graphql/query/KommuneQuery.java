package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class KommuneQuery {

    @Autowired
    private KommuneRepository kommuneRepository;

    @QueryMapping
    public List<Kommune> kommuner() {
        return StreamSupport.stream(kommuneRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Kommune::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Kommune kommuneByKommunenummer(@Argument String kommunenummer) {
        return kommuneRepository.findByKommunenummer(kommunenummer).orElse(null);
    }
}
