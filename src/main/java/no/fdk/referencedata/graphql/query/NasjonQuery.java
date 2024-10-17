package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.Nasjon;
import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.NasjonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class NasjonQuery {

    @Autowired
    private NasjonService nasjonService;

    @QueryMapping
    public List<Nasjon> nasjoner() {
        return nasjonService.getNasjoner().stream()
                .sorted(Comparator.comparing(Nasjon::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Nasjon nasjonByNasjonsnummer(@Argument String nasjonsnummer) {
        return nasjonService.getNasjonByNasjonsnummer(nasjonsnummer).orElse(null);
    }
}
