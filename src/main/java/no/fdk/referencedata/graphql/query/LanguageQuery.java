package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.language.Language;
import no.fdk.referencedata.eu.language.LanguageRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class LanguageQuery {

    private final LanguageRepository languageRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<Language> languages() {
        return support.allSortedByUri(languageRepository, Language::getUri);
    }

    @QueryMapping
    public Language languageByCode(@Argument String code) {
        return support.byCode(languageRepository::findByCode, code);
    }
}
