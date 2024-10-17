package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class FylkeQuery {

    @Autowired
    private FylkeRepository fylkeRepository;

    @QueryMapping
    public List<Fylke> fylker() {
        return StreamSupport.stream(fylkeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Fylke::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Fylke fylkeByFylkesnummer(@Argument String fylkesnummer) {
        return fylkeRepository.findByFylkesnummer(fylkesnummer).orElse(null);
    }
}
