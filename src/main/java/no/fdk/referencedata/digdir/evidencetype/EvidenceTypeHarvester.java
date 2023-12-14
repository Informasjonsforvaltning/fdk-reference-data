package no.fdk.referencedata.digdir.evidencetype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.EvidenceTypeVocabulary;
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
public class EvidenceTypeHarvester extends AbstractDataNorgeHarvester<EvidenceType> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static final String PATH = "evidence-type";
    private static String VERSION = "0";

    public EvidenceTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<EvidenceType> harvest() {
        log.info("Starting harvest of data.norge evidence-types");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch evidence-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EvidenceTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapEvidenceType);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(EvidenceTypeVocabulary.getURI()),
                DCTerms.modified
        ).getString();
    }

    private EvidenceType mapEvidenceType(Resource evidenceType) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(evidenceType.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return EvidenceType.builder()
                .uri(evidenceType.getURI())
                .code(evidenceType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
