package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class EuroVocQuery {

    @Autowired
    private EuroVocRepository euroVocRepository;

    @QueryMapping
    public List<EuroVoc> euroVocs() {
        return StreamSupport.stream(euroVocRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(EuroVoc::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public EuroVoc euroVocByCode(@Argument String code) {
        return euroVocRepository.findByCode(code).orElse(null);
    }
}
