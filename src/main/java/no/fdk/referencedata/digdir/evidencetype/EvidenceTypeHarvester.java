package no.fdk.referencedata.digdir.evidencetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.EvidenceTypeVocabulary;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Slf4j
public class EvidenceTypeHarvester extends AbstractDataNorgeHarvester<EvidenceType> {

    private static final String PATH = "evidence-type";

    public EvidenceTypeHarvester() {
        super();
    }


    public Flux<EvidenceType> harvest() {
        log.info("Starting harvest of data.norge evidence-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch evidence-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EvidenceTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapEvidenceType);
    }


    private EvidenceType mapEvidenceType(Resource evidenceType) {
        Map<String, String> label = SkosMapper.extractLabels(evidenceType);

        return EvidenceType.builder()
                .uri(evidenceType.getURI())
                .code(evidenceType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
