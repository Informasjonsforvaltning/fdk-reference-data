package no.fdk.referencedata.digdir.serviceoutputtype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.ServiceOutputTypeVocabulary;
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
public class ServiceOutputTypeHarvester extends AbstractDataNorgeHarvester<ServiceOutputType> {

    private static final String PATH = "service-output-type";

    public ServiceOutputTypeHarvester() {
        super();
    }


    public Flux<ServiceOutputType> harvest() {
        log.info("Starting harvest of data.norge service-output-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch service-output-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        ServiceOutputTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapServiceOutputType);
    }


    private ServiceOutputType mapServiceOutputType(Resource serviceOutputType) {
        Map<String, String> label = SkosMapper.extractLabels(serviceOutputType);

        return ServiceOutputType.builder()
                .uri(serviceOutputType.getURI())
                .code(serviceOutputType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
