package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.licence.Licence;
import no.fdk.referencedata.eu.licence.LicenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class LicenceQuery {

    private final LicenceRepository licenceRepository;

    @Autowired
    public LicenceQuery(LicenceRepository licenceRepository) {
        this.licenceRepository = licenceRepository;
    }

    @QueryMapping
    public List<Licence> licences() {
        return StreamSupport.stream(licenceRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Licence::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Licence licenceByCode(@Argument String code) {
        return licenceRepository.findByCode(code).orElse(null);
    }
}
