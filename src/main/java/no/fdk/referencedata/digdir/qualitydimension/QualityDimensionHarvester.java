package no.fdk.referencedata.digdir.qualitydimension;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.QualityDimensionVocabulary;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
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
public class QualityDimensionHarvester extends AbstractDataNorgeHarvester<QualityDimension> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static final String PATH = "quality-dimension";
    private static String VERSION = "0";

    public QualityDimensionHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<QualityDimension> harvest() {
        log.info("Starting harvest of data.norge quality-dimensions");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch quality-dimensions"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        QualityDimensionVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapQualityDimension);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(QualityDimensionVocabulary.getURI()),
                DCTerms.modified
        ).getString();
    }

    private QualityDimension mapQualityDimension(Resource qualityDimension) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(qualityDimension.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return QualityDimension.builder()
                .uri(qualityDimension.getURI())
                .code(qualityDimension.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
