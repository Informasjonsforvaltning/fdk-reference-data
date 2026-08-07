package no.fdk.referencedata.mobility.datastandard;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mobility.AbstractMobilityHarvester;
import no.fdk.referencedata.mobility.vocabulary.MobilityDataStandardVocabulary;
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
public class MobilityDataStandardHarvester extends AbstractMobilityHarvester<MobilityDataStandard> {

    private static final String PATH = "mobility-data-standard/latest/mobility-data-standard.ttl";

    public MobilityDataStandardHarvester() {
        super();
    }


    public Flux<MobilityDataStandard> harvest() {
        log.info("Starting harvest of mobility data standards");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch mobility data standard source"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        MobilityDataStandardVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapMobilityDataStandard);
    }


    private MobilityDataStandard mapMobilityDataStandard(Resource standard) {
        Map<String, String> label = SkosMapper.extractLabels(standard);

        String[] uriParts = standard.getURI().split("/");

        return MobilityDataStandard.builder()
                .uri(standard.getURI())
                .code(uriParts[uriParts.length - 1])
                .label(label)
                .build();
    }
}
