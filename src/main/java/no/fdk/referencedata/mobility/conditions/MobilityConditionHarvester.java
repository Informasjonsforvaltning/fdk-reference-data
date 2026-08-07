package no.fdk.referencedata.mobility.conditions;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.mobility.AbstractMobilityHarvester;
import no.fdk.referencedata.mobility.vocabulary.MobilityConditionsVocabulary;
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
public class MobilityConditionHarvester extends AbstractMobilityHarvester<MobilityCondition> {

    private static final String PATH = "conditions-for-access-and-usage/latest/conditions-for-access-and-usage.ttl";

    public MobilityConditionHarvester() {
        super();
    }


    public Flux<MobilityCondition> harvest() {
        log.info("Starting harvest of conditions for access and usage");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch conditions for access and usage source"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        MobilityConditionsVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapMobilityCondition);
    }


    private MobilityCondition mapMobilityCondition(Resource condition) {
        Map<String, String> label = SkosMapper.extractLabels(condition);

        String[] uriParts = condition.getURI().split("/");

        return MobilityCondition.builder()
                .uri(condition.getURI())
                .code(uriParts[uriParts.length - 1])
                .label(label)
                .build();
    }
}
