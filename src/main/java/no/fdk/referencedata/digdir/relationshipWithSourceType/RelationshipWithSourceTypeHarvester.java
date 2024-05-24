package no.fdk.referencedata.digdir.relationshipWithSourceType;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.digdir.AbstractDataNorgeHarvester;
import no.fdk.referencedata.digdir.vocabulary.RelationshipWithSourceTypeVocabulary;
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
public class RelationshipWithSourceTypeHarvester extends AbstractDataNorgeHarvester<RelationshipWithSourceType> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .toList();
    private static final String PATH = "relationship-with-source-type";
    private static String VERSION = "0";

    public RelationshipWithSourceTypeHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<RelationshipWithSourceType> harvest() {
        log.info("Starting harvest of data.norge relationship-with-source-type");
        final org.springframework.core.io.Resource rdfSource = getSource(PATH);
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch relationship-with-source-type distribution"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(getModel())
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        RelationshipWithSourceTypeVocabulary.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapRelationshipWithSourceType);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(RelationshipWithSourceTypeVocabulary.getURI()),
                DCTerms.modified
        ).getString();
    }

    private RelationshipWithSourceType mapRelationshipWithSourceType(Resource relationshipWithSourceType) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(relationshipWithSourceType.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return RelationshipWithSourceType.builder()
                .uri(relationshipWithSourceType.getURI())
                .code(relationshipWithSourceType.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
