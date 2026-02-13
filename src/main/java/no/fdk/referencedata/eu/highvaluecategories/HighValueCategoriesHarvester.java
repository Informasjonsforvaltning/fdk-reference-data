package no.fdk.referencedata.eu.highvaluecategories;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.*;
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
public class HighValueCategoriesHarvester extends AbstractEuHarvester<HighValueCategory> {
    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";
    private static final String SCHEMA_URI = "http://data.europa.eu/bna/asd487ae75";

    public HighValueCategoriesHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<HighValueCategory> harvest() {
        log.info("Starting harvest of EU high-value categories");
        final org.springframework.core.io.Resource categoriesRdfSource = getSource();
        if(categoriesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch high-value categories distribution"));
        }

        return Mono.justOrEmpty(loadModel(categoriesRdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme, ResourceFactory.createResource(SCHEMA_URI)).toList())
                .filter(Resource::isURIResource)
                .map(this::mapHighValueCategory);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource(SCHEMA_URI),
                OWL.versionInfo
        ).getString();
    }

    private HighValueCategory mapHighValueCategory(Resource highValueCategory) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(highValueCategory.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return HighValueCategory.builder()
                .uri(highValueCategory.getURI())
                .code(highValueCategory.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX dct: <http://purl.org/dc/terms/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#> " +
            "CONSTRUCT { " +
                "<" + SCHEMA_URI  + "> owl:versionInfo ?version . " +
                "?category skos:inScheme <" + SCHEMA_URI + "> . " +
                "?category dc:identifier ?code . " +
                "?category skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
                "<" + SCHEMA_URI + "> owl:versionInfo ?version . " +
                "?category skos:inScheme <" + SCHEMA_URI + "> . " +
                "?category a skos:Concept . " +
                "?category dc:identifier ?code . " +
                "?category skos:prefLabel ?prefLabel . " +
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
