package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.Nasjon;
import no.fdk.referencedata.geonorge.administrativeenheter.nasjon.NasjonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class NasjonQuery implements GraphQLQueryResolver {

    @Autowired
    private NasjonService nasjonService;

    public List<Nasjon> getNasjoner() {
        return nasjonService.getNasjoner().stream()
                .sorted(Comparator.comparing(Nasjon::getUri))
                .collect(Collectors.toList());
    }

    public Nasjon getNasjonByNasjonsnummer(String nasjonsnummer) {
        return nasjonService.getNasjonByNasjonsnummer(nasjonsnummer).orElse(null);
    }
}