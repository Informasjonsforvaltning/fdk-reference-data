package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.vocabulary.FDK;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class EuroVocHarvester extends AbstractEuHarvester<EuroVoc> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .toList();
    private static final String VERSION = "0";

    public EuroVocHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<EuroVoc> harvest() {
        log.info("Starting harvest of EU eurovoc");
        final org.springframework.core.io.Resource source = getSource();
        if (source == null) {
            return Flux.error(new Exception("Unable to fetch eurovoc distribution"));
        }

        return Mono.justOrEmpty(loadModel(source, true))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, SKOS.Concept).toList())
                .filter(Resource::isURIResource)
                .map(this::mapEuroVoc);
    }

    private List<URI> uriListFromStatements(List<Statement> statements) {
        return statements.stream()
                .map(stmt -> {
                    try {
                        return new URI(stmt.getObject().toString());
                    } catch (URISyntaxException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private EuroVoc mapEuroVoc(Resource euroVoc) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(euroVoc.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return EuroVoc.builder()
                .uri(euroVoc.getURI())
                .code(euroVoc.getURI().substring(euroVoc.getURI().lastIndexOf("/") + 1))
                .label(label)
                .children(uriListFromStatements(euroVoc.listProperties(SKOS.narrower).toList()))
                .parents(uriListFromStatements(euroVoc.listProperties(SKOS.broader).toList()))
                .eurovocPaths(euroVoc.listProperties(FDK.themePath).mapWith(Statement::getString).toList())
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX cdm: <http://publications.europa.eu/ontology/cdm#> " +
            "CONSTRUCT { " +
                "?euroVoc a skos:Concept . " +
                "?euroVoc a cdm:concept_eurovoc . " +
                "?euroVoc skos:broader ?vocBroader . " +
                "?euroVoc skos:narrower ?vocNarrower . " +
                "?euroVoc skos:prefLabel ?vocLabel . " +
            "} WHERE { " +
                "?euroVoc a cdm:concept_eurovoc . " +
                "OPTIONAL { ?euroVoc skos:broader ?vocBroader } . " +
                "OPTIONAL { ?euroVoc skos:narrower ?vocNarrower } . " +
                "?euroVoc skos:prefLabel ?vocLabel . " +
                "FILTER(" +
                    "LANG(?vocLabel) = 'en' || " +
                    "LANG(?vocLabel) = 'no' || " +
                    "LANG(?vocLabel) = 'nb' || " +
                    "LANG(?vocLabel) = 'nn'" +
                ") . " +
            "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
