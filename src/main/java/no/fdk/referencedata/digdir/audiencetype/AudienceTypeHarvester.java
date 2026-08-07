package no.fdk.referencedata.digdir.audiencetype;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.AudienceTypeVocabulary;
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

@Component
@Slf4j
public class AudienceTypeHarvester extends AbstractDataNorgeHarvester<AudienceType> {

    private static final String PATH = "audience-type";

    public AudienceTypeHarvester() {
        super();
    }


    public Flux<AudienceType> harvest() {
        log.info("Starting harvest of data.norge audience-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch audience-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        AudienceTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapAudienceType);
    }


    private AudienceType mapAudienceType(Resource audienceType) {
        Map<String, String> label = SkosMapper.extractLabels(audienceType);

        return AudienceType.builder()
                .uri(audienceType.getURI())
                .code(audienceType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
