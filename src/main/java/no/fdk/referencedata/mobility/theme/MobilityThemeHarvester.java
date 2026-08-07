package no.fdk.referencedata.mobility.theme;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mobility.AbstractMobilityHarvester;
import no.fdk.referencedata.mobility.vocabulary.MobilityThemeVocabulary;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MobilityThemeHarvester extends AbstractMobilityHarvester<MobilityTheme> {

    private static final String PATH = "mobility-theme/latest/mobility-theme.ttl";

    public MobilityThemeHarvester() {
        super();
    }


    public Flux<MobilityTheme> harvest() {
        log.info("Starting harvest of mobility themes");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch mobility theme source"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        MobilityThemeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapMobilityTheme);
    }


    private MobilityTheme mapMobilityTheme(Resource theme) {
        Map<String, String> label = SkosMapper.extractLabels(theme);

        String[] uriParts = theme.getURI().split("/");

        return MobilityTheme.builder()
                .uri(theme.getURI())
                .code(uriParts[uriParts.length - 1])
                .label(label)
                .build();
    }
}
