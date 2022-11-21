package no.fdk.referencedata.eu.mainactivity;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EUMainActivity;
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
public class MainActivityHarvester extends AbstractEuHarvester<MainActivity> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());
    private static String VERSION = "0";

    public MainActivityHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<MainActivity> harvest() {
        log.info("Starting harvest of EU main-activity");
        final org.springframework.core.io.Resource rdfSource = getSource(sparqlQuery());
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch main-activity distribution"));
        }

        return Mono.justOrEmpty(getModel(rdfSource))
                .doOnSuccess(this::updateVersion)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EUMainActivity.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapMainActivity);
    }

    private void updateVersion(Model m) {
        VERSION = m.getProperty(
                m.getResource("http://publications.europa.eu/resource/authority/main-activity"),
                OWL.versionInfo
        ).getString();
    }

    private MainActivity mapMainActivity(Resource mainActivity) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(mainActivity.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return MainActivity.builder()
                .uri(mainActivity.getURI())
                .code(mainActivity.getProperty(DC.identifier).getObject().toString())
                .label(label)
                .build();
    }

    private String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "CONSTRUCT { " +
                "atres:main-activity owl:versionInfo ?version . " +
                "?mainActivity skos:inScheme atres:main-activity . " +
                "?mainActivity dc:identifier ?code . " +
                "?mainActivity skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
                "atres:main-activity owl:versionInfo ?version . " +
                "?mainActivity skos:inScheme atres:main-activity . " +
                "?mainActivity a skos:Concept . " +
                "?mainActivity dc:identifier ?code . " +
                "?mainActivity skos:prefLabel ?prefLabel . " +
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
