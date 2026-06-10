package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.language.Language;
import no.fdk.referencedata.eu.language.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class LanguageQuery {

    @Autowired
    private LanguageRepository languageRepository;

    @QueryMapping
    public List<Language> languages() {
        return StreamSupport.stream(languageRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(Language::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public Language languageByCode(@Argument String code) {
        return languageRepository.findByCode(code).orElse(null);
    }
}
