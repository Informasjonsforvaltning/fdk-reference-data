package no.fdk.referencedata.eu.licence;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EULicence;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.vocabulary.AT;
import no.fdk.referencedata.vocabulary.EUVOC;
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
import java.util.*;

@Component
@Slf4j
public class LicenceHarvester extends AbstractEuHarvester<Licence> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .toList();
    private static String VERSION = "0";

    public LicenceHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<Licence> harvest() {
        log.info("Starting harvest of EU licences");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch licence distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                    EULicence.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapLicence);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/licence"),
                OWL.versionInfo
        ).getString();
    }

    private Licence mapLicence(Resource licence) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(licence.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        final Map<String, String> definition = new HashMap<>();
        Flux.fromIterable(licence.listProperties(SKOS.definition).toList())
            .map(stmt -> stmt.getObject().asLiteral())
            .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
            .doOnNext(literal -> definition.put(literal.getLanguage(), literal.getString()))
            .subscribe();

        final List<String> context = new ArrayList<>();
        Flux.fromIterable(licence.listProperties(EUVOC.context).toList())
            .map(stmt -> stmt.getObject().asResource())
            .map(Resource::getURI)
            .doOnNext(uri -> context.add(uri.substring(uri.lastIndexOf("/") + 1)))
            .subscribe();

        return Licence.builder()
                .uri(licence.getURI())
                .code(licence.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .definition(definition)
                .deprecated(licence.getProperty(AT.deprecated).getBoolean())
                .context(context)
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX at: <http://publications.europa.eu/ontology/authority/> " +
            "CONSTRUCT { " +
            "atres:licence owl:versionInfo ?version . " +
            "atres:licence skos:prefLabel ?schemaLabel . " +
            "?licence skos:inScheme ?inScheme . " +
            "?licence dc:identifier ?code . " +
            "?licence skos:definition ?definition . " +
            "?licence at:start.use ?startUse . " +
            "?licence at:deprecated ?deprecated . " +
            "?licence skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
            "atres:licence owl:versionInfo ?version . " +
            "atres:licence skos:prefLabel ?schemaLabel . " +
            "FILTER(" +
            "LANG(?schemaLabel) = 'en' || " +
            "LANG(?schemaLabel) = 'no' || " +
            "LANG(?schemaLabel) = 'nb' || " +
            "LANG(?schemaLabel) = 'nn'" +
            ") . " +
            "?licence skos:inScheme atres:licence . " +
            "?licence skos:inScheme ?inScheme . " +
            "?licence a skos:Concept . " +
            "?licence dc:identifier ?code . " +
            "FILTER(?code != 'OP_DATPRO') . " +
            "OPTIONAL { ?licence at:start.use ?startUse . } " +
            "?licence skos:definition ?definition . " +
            "?licence at:deprecated ?deprecated . " +
            "?licence skos:prefLabel ?prefLabel . " +
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
