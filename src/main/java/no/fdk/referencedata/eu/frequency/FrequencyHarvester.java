package no.fdk.referencedata.eu.frequency;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUFrequency;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
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
public class FrequencyHarvester extends AbstractEuHarvester<Frequency> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static final String cellarURI = "http://publications.europa.eu/resource/cellar/c7429320-f70c-11ec-b94a-01aa75ed71a1.0001.02/DOC_1";
    private static final String rdfFileName = "frequencies-skos.rdf";
    private static String VERSION = "0";

    public FrequencyHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<Frequency> harvest() {
        log.info("Starting harvest of EU frequencies");
        final org.springframework.core.io.Resource rdfSource = getSource(cellarURI, rdfFileName);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch frequency distribution"));
        }

        return Mono.justOrEmpty(getModel(rdfSource))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUFrequency.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapFrequency);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/frequency"),
                OWL.versionInfo
        ).getString();
    }

    private Frequency mapFrequency(Resource frequency) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(frequency.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return Frequency.builder()
                .uri(frequency.getURI())
                .code(frequency.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }
}
