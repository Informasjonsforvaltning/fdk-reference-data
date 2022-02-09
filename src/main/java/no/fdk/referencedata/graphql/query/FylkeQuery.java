package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.Fylke;
import no.fdk.referencedata.geonorge.administrativeenheter.fylke.FylkeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class FylkeQuery implements GraphQLQueryResolver {

    @Autowired
    private FylkeRepository fylkeRepository;

    public List<Fylke> getFylker() {
        return StreamSupport.stream(fylkeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Fylke::getUri))
                .collect(Collectors.toList());
    }

    public Fylke getFylkeByFylkesnummer(String fylkesnummer) {
        return fylkeRepository.findByFylkesnummer(fylkesnummer).orElse(null);
    }
}