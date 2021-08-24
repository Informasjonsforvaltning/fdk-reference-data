package no.fdk.referencedata.eu.datatheme;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.eu.vocabulary.EUDataTheme;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataThemeHarvester extends AbstractEuHarvester<DataTheme> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    public DataThemeHarvester() {
        super("data-theme", "skos_ap_act/data-theme-skos-ap-act.rdf");
    }

    public Flux<DataTheme> harvest() {
        log.info("Starting harvest of EU data themes");
        final org.springframework.core.io.Resource dataThemesRdfSource = getSource();
        if(dataThemesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch data-theme distribution"));
        }

        final AtomicInteger count = new AtomicInteger();

        return Mono.justOrEmpty(getModel(dataThemesRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUDataTheme.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapDataTheme)
                .doOnNext(item -> count.getAndIncrement())
                .doFinally(signal -> log.info("Successfully harvested {} EU data themes", count.get()));
    }

    private DataTheme mapDataTheme(Resource dataTheme) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(dataTheme.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return DataTheme.builder()
                .uri(dataTheme.getURI())
                .code(dataTheme.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }
}
