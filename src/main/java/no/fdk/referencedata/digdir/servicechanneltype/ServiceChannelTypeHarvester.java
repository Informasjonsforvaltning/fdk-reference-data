package no.fdk.referencedata.digdir.servicechanneltype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.ServiceChannelTypeVocabulary;
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
public class ServiceChannelTypeHarvester extends AbstractDataNorgeHarvester<ServiceChannelType> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static final String PATH = "service-channel-type";
    private static String VERSION = "0";

    public ServiceChannelTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<ServiceChannelType> harvest() {
        log.info("Starting harvest of data.norge service-channel-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch service-channel-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        ServiceChannelTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapServiceChannelType);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(ServiceChannelTypeVocabulary.getURI()),
                DCTerms.modified
        ).getString();
    }

    private ServiceChannelType mapServiceChannelType(Resource serviceChannelType) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(serviceChannelType.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ServiceChannelType.builder()
                .uri(serviceChannelType.getURI())
                .code(serviceChannelType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
