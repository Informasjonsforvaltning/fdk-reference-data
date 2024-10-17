package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class DataThemeQuery {

    @Autowired
    private DataThemeRepository dataThemeRepository;

    @QueryMapping
    public List<DataTheme> dataThemes() {
        return StreamSupport.stream(dataThemeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(DataTheme::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public DataTheme dataThemeByCode(@Argument String code) {
        return dataThemeRepository.findByCode(code).orElse(null);
    }
}
