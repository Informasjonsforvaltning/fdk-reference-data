package no.fdk.referencedata.eu.eurovoc;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.util.ZipUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EuroVocHarvester extends AbstractEuHarvester<EuroVoc> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public EuroVocHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<EuroVoc> harvest() {
        log.info("Starting harvest of EU eurovoc");
        final org.springframework.core.io.Resource source = getSource();
        if(source == null) {
            return Flux.error(new Exception("Unable to fetch eurovoc distribution"));
        }

        return Mono.justOrEmpty(loadModel(source))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, SKOS.Concept).toList())
                .filter(Resource::isURIResource)
                .map(this::mapEuroVoc);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/ontology/euvoc#EuroVoc"),
                OWL.versionInfo
        ).getString();
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
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#> " +
            "CONSTRUCT { " +
                "euvoc:EuroVoc owl:versionInfo ?version . " +
                "?euroVoc a skos:Concept . " +
                "?euroVoc skos:prefLabel ?vocLabel . " +
                "?domain a skos:Concept . " +
                "?domain skos:prefLabel ?domainLabel . " +
            "} WHERE { " +
                "euvoc:EuroVoc owl:equivalentClass ?eqClass . " +
                "?eqClass owl:hasValue ?eqVoc . " +
                "?eqVoc owl:versionInfo ?version . " +
                "?euroVoc skos:inScheme ?eqVoc . " +
                "?euroVoc a skos:Concept . " +
                "?euroVoc skos:prefLabel ?vocLabel . " +
                "FILTER(" +
                    "LANG(?vocLabel) = 'en' || " +
                    "LANG(?vocLabel) = 'no' || " +
                    "LANG(?vocLabel) = 'nb' || " +
                    "LANG(?vocLabel) = 'nn'" +
                ") . " +
                "?domain skos:inScheme <http://eurovoc.europa.eu/domains> . " +
                "?domain a skos:Concept . " +
                "?domain skos:prefLabel ?domainLabel . " +
                "FILTER(" +
                    "LANG(?domainLabel) = 'en' || " +
                    "LANG(?domainLabel) = 'no' || " +
                    "LANG(?domainLabel) = 'nb' || " +
                    "LANG(?domainLabel) = 'nn'" +
                ") . " +
            "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
