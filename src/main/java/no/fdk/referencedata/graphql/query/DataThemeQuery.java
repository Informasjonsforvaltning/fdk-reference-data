package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.datatheme.DataTheme;
import no.fdk.referencedata.eu.datatheme.DataThemeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DataThemeQuery {

    private final DataThemeRepository dataThemeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<DataTheme> dataThemes() {
        return support.allSortedByUri(dataThemeRepository, DataTheme::getUri);
    }

    @QueryMapping
    public DataTheme dataThemeByCode(@Argument String code) {
        return support.byCode(dataThemeRepository::findByCode, code);
    }
}
