package no.fdk.referencedata.eu.language;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EULanguage;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
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

import static no.fdk.referencedata.i18n.Language.ENGLISH;
import static no.fdk.referencedata.i18n.Language.NORWEGIAN;

@Component
@Slf4j
public class LanguageHarvester extends AbstractEuHarvester<Language> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(no.fdk.referencedata.i18n.Language.values())
                    .map(no.fdk.referencedata.i18n.Language::code)
                    .toList();

    public LanguageHarvester() {
        super();
    }


    public Flux<Language> harvest() {
        log.info("Starting harvest of EU languages");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch language distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EULanguage.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapLanguage);
    }

    private Language mapLanguage(Resource language) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(language.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        String code = language.getProperty(DC.identifier).getObject().toString();

        switch (code) {
            case "NOB":
                label.put(NORWEGIAN.code(), "norsk (bokmål)");
                break;
            case "NNO":
                label.put(NORWEGIAN.code(), "norsk (nynorsk)");
                break;
            case "SMI":
                label.put(NORWEGIAN.code(), "samisk");
                break;
            case "SMJ":
                label.put(NORWEGIAN.code(), "lulesamisk");
                label.put(ENGLISH.code(), "Lule Sami");
                break;
            case "SMA":
                label.put(NORWEGIAN.code(), "sørsamisk");
                label.put(ENGLISH.code(), "Southern Sami");
                break;
            case "SOM":
                label.put(NORWEGIAN.code(), "somali");
                break;
        }

        return Language.builder()
                .uri(language.getURI())
                .code(code)
                .label(label)
                .build();
    }

    public String sparqlQuery() {
        String query = """
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dc: <http://purl.org/dc/elements/1.1/>
                PREFIX atres: <http://publications.europa.eu/resource/authority/>
                CONSTRUCT {
                    ?item skos:inScheme atres:language .
                    ?item dc:identifier ?code .
                    ?item skos:prefLabel ?prefLabel .
                } WHERE {
                    atres:language skos:hasTopConcept ?item .
                    ?item dc:identifier ?code .
                    FILTER(?code != 'OP_DATPRO') .
                    ?item skos:prefLabel ?prefLabel .
                    FILTER(
                        LANG(?prefLabel) = 'en' ||
                        LANG(?prefLabel) = 'no' ||
                        LANG(?prefLabel) = 'nb' ||
                        LANG(?prefLabel) = 'nn' ||
                        LANG(?prefLabel) = 'sma' ||
                        LANG(?prefLabel) = 'smj'
                    ) .
                }""";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
