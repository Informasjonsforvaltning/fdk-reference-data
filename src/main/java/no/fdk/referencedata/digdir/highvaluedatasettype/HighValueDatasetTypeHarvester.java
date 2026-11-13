package no.fdk.referencedata.digdir.highvaluedatasettype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.HighValueDatasetTypeVocabulary;
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
public class HighValueDatasetTypeHarvester extends AbstractDataNorgeHarvester<HighValueDatasetType> {

    private static final String PATH = "high-value-dataset-type";

    public HighValueDatasetTypeHarvester() {
        super();
    }


    public Flux<HighValueDatasetType> harvest() {
        log.info("Starting harvest of data.norge high-value-dataset-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch high-value-dataset-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        HighValueDatasetTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapHighValueDatasetType);
    }


    private HighValueDatasetType mapHighValueDatasetType(Resource highValueDatasetType) {
        Map<String, String> label = SkosMapper.extractLabels(highValueDatasetType);

        return HighValueDatasetType.builder()
                .uri(highValueDatasetType.getURI())
                .code(highValueDatasetType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
