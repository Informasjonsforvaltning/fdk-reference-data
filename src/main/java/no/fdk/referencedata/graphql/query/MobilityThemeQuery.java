package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.mobility.theme.MobilityTheme;
import no.fdk.referencedata.mobility.theme.MobilityThemeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MobilityThemeQuery {

    private final MobilityThemeRepository mobilityThemeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<MobilityTheme> mobilityThemes() {
        return support.allSortedByUri(mobilityThemeRepository, MobilityTheme::getUri);
    }

    @QueryMapping
    public MobilityTheme mobilityThemeByCode(@Argument String code) {
        return support.byCode(mobilityThemeRepository::findByCode, code);
    }
}
