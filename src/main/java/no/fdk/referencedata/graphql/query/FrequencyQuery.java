package no.fdk.referencedata.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.frequency.Frequency;
import no.fdk.referencedata.eu.frequency.FrequencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class FrequencyQuery implements GraphQLQueryResolver {

    @Autowired
    private FrequencyRepository frequencyRepository;

    public List<Frequency> getFrequencies() {
        return StreamSupport.stream(frequencyRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Frequency::getUri))
                .collect(Collectors.toList());
    }

    public Frequency getFrequencyByCode(String code) {
        return frequencyRepository.findByCode(code).orElse(null);
    }
}