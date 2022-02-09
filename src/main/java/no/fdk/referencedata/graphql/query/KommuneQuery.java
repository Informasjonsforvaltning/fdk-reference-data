package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.Kommune;
import no.fdk.referencedata.geonorge.administrativeenheter.kommune.KommuneRepository;
import no.fdk.referencedata.iana.mediatype.MediaType;
import no.fdk.referencedata.iana.mediatype.MediaTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class KommuneQuery implements GraphQLQueryResolver {

    @Autowired
    private KommuneRepository kommuneRepository;

    public List<Kommune> getKommuner() {
        return StreamSupport.stream(kommuneRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Kommune::getUri))
                .collect(Collectors.toList());
    }

    public Kommune getKommuneByKommunenummer(String kommunenummer) {
        return kommuneRepository.findByKommunenummer(kommunenummer).orElse(null);
    }
}