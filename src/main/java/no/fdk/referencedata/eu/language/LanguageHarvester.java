package no.fdk.referencedata.eu.language;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EULanguage;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LanguageHarvester extends AbstractEuHarvester<Language> {


    private final Map<String, Map<String, String>> missingTranslations = Map.ofEntries(
            Map.entry(EULanguage.getURI() + "/NOB", Map.of("no", "norsk (bokmål)")),
            Map.entry(EULanguage.getURI() + "/NNO", Map.of("no", "norsk (nynorsk)")),
            Map.entry(EULanguage.getURI() + "/SMI", Map.of("no", "samisk")),
            Map.entry(EULanguage.getURI() + "/SMJ", Map.of(
                    "no", "lulesamisk",
                    "en", "Lule Sami"
            )),
            Map.entry(EULanguage.getURI() + "/SMA", Map.of(
                    "no", "sørsamisk",
                    "en", "Southern Sami"
            )),
            Map.entry(EULanguage.getURI() + "/SOM", Map.of("no", "somali"))
    );

    public LanguageHarvester() {
        super();
    }

    @Override
    protected Model translate(Model model) {
        return addMissingTranslations(model, SKOS.prefLabel, missingTranslations);
    }

    public Flux<Language> harvest() {
        log.info("Starting harvest of EU languages");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch language distribution"));
        }

        return Mono.justOrEmpty(loadModel(rdfSource, false))
                .map(this::translate)
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EULanguage.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapLanguage);
    }

    private Language mapLanguage(Resource language) {
        return Language.builder()
                .uri(language.getURI())
                .code(language.getProperty(DC.identifier).getObject().toString())
                .label(SkosMapper.extractLabels(language))
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
