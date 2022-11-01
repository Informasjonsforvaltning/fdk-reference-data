package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class DataThemeQuery implements GraphQLQueryResolver {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    public List<DataTheme> getDataThemes() {
        return StreamSupport.stream(dataThemeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DataTheme::getUri))
                .collect(Collectors.toList());
    }

    public DataTheme getDataThemeByCode(String code) {
        return dataThemeRepository.findByCode(code).orElse(null);
    }
}