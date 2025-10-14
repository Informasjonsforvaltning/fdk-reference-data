package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.mobility.theme.MobilityTheme;
import no.fdk.referencedata.mobility.theme.MobilityThemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class MobilityThemeQuery {
    private final MobilityThemeRepository mobilityThemeRepository;

    @Autowired
    public MobilityThemeQuery(MobilityThemeRepository mobilityThemeRepository) {
        this.mobilityThemeRepository = mobilityThemeRepository;
    }

    @QueryMapping
    public List<MobilityTheme> mobilityThemes() {
        return StreamSupport.stream(mobilityThemeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(MobilityTheme::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public MobilityTheme mobilityThemeByCode(@Argument String code) {
        return mobilityThemeRepository.findByCode(code).orElse(null);
    }
}
