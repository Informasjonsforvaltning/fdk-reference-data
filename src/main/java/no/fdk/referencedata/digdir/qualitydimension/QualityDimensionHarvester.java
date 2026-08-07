package no.fdk.referencedata.digdir.qualitydimension;

import no.fdk.referencedata.rdf.SkosMapper;

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

    private static final String PATH = "quality-dimension";

    public QualityDimensionHarvester() {
        super();
    }


    public Flux<QualityDimension> harvest() {
        log.info("Starting harvest of data.norge quality-dimensions");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch quality-dimensions"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        QualityDimensionVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapQualityDimension);
    }


    private QualityDimension mapQualityDimension(Resource qualityDimension) {
        Map<String, String> label = SkosMapper.extractLabels(qualityDimension);

        return QualityDimension.builder()
                .uri(qualityDimension.getURI())
                .code(qualityDimension.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
