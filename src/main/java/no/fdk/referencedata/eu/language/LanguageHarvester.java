package no.fdk.referencedata.eu.language;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.eu.vocabulary.EULanguage;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LanguageHarvester extends AbstractEuHarvester<Language> {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(no.fdk.referencedata.i18n.Language.values())
                    .map(no.fdk.referencedata.i18n.Language::code)
                    .toList();

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

    public Model translateLanguages(Model model) {
        Model translated = ModelFactory.createDefaultModel();
        model.listStatements().forEach(translated::add);

        for (String subject : missingTranslations.keySet()) {
            Resource subjectResource = model.getResource(subject);
            Map<String, String> subjectTranslations = missingTranslations.get(subject);
            for (Map.Entry<String, String> entry : subjectTranslations.entrySet()) {
                translated.add(
                        subjectResource,
                        SKOS.prefLabel,
                        entry.getValue(),
                        entry.getKey()
                );
            }
        }

        updateModel(translated);
        return translated;
    }

    private Optional<Model> loadAndTranslateModel(org.springframework.core.io.Resource rdfSource) {
        return loadModel(rdfSource, false)
                .map(this::translateLanguages);
    }

    public Flux<Language> harvest() {
        log.info("Starting harvest of EU languages");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch language distribution"));
        }

        return Mono.justOrEmpty(loadAndTranslateModel(rdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(SKOS.inScheme,
                        EULanguage.SCHEME).toList())
                .filter(Resource::isURIResource)
                .map(this::mapLanguage);
    }

    private Language mapLanguage(Resource language) {
        return Language.builder()
                .uri(language.getURI())
                .code(language.getProperty(DC.identifier).getObject().toString())
                .label(language.listProperties(SKOS.prefLabel).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
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
