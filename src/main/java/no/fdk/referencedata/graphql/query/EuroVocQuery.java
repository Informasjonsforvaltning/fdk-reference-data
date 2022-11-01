package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.eurovoc.EuroVoc;
import no.fdk.referencedata.eu.eurovoc.EuroVocRepository;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class EuroVocQuery implements GraphQLQueryResolver {

    @Autowired
    private EuroVocRepository euroVocRepository;

    public List<EuroVoc> getEuroVocs() {
        return StreamSupport.stream(euroVocRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(EuroVoc::getUri))
                .collect(Collectors.toList());
    }

    public EuroVoc getEuroVocByCode(String code) {
        return euroVocRepository.findByCode(code).orElse(null);
    }
}