package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.frequency.Frequency;
import no.fdk.referencedata.eu.frequency.FrequencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class FrequencyQuery {

    @Autowired
    private FrequencyRepository frequencyRepository;

    @QueryMapping
    public List<Frequency> frequencies() {
        return StreamSupport.stream(frequencyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Frequency::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Frequency frequencyByCode(@Argument String code) {
        return frequencyRepository.findByCode(code).orElse(null);
    }
}
