package no.fdk.referencedata.eu.conceptstatus;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUConceptStatus;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConceptStatusHarvester extends AbstractEuHarvester<ConceptStatus> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public ConceptStatusHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<ConceptStatus> harvest() {
        log.info("Starting harvest of EU concept status");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch concept status distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUConceptStatus.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapConceptStatus);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(EUConceptStatus.getURI()),
                OWL.versionInfo
        ).getString();
    }

    private ConceptStatus mapConceptStatus(Resource status) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(status.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ConceptStatus.builder()
                .uri(status.getURI())
                .code(status.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "CONSTRUCT { " +
                "atres:concept-status owl:versionInfo ?version . " +
                "?status skos:inScheme atres:concept-status . " +
                "?status dc:identifier ?code . " +
                "?status skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
                "atres:concept-status owl:versionInfo ?version . " +
                "?status skos:inScheme atres:concept-status . " +
                "?status a skos:Concept . " +
                "?status dc:identifier ?code . " +
                "?status skos:prefLabel ?prefLabel . " +
                "FILTER(" +
                    "LANG(?prefLabel) = 'en' || " +
                    "LANG(?prefLabel) = 'no' || " +
                    "LANG(?prefLabel) = 'nb' || " +
                    "LANG(?prefLabel) = 'nn'" +
                ") . " +
            "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
