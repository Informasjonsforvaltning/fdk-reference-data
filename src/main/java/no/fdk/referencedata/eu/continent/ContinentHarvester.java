package no.fdk.referencedata.eu.continent;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
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
public class ContinentHarvester extends AbstractEuHarvester<Continent> {
    private static final String CONTINENT_URI = "http://publications.europa.eu/resource/authority/continent/";

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private final Map<String, Map<String, String>> missingTranslations = Map.ofEntries(
            Map.entry(CONTINENT_URI + "AFRICA", Map.of("no", "Afrika")),
            Map.entry(CONTINENT_URI + "AMERICA", Map.of("no", "Amerika")),
            Map.entry(CONTINENT_URI + "ASIA", Map.of("no", "Asia")),
            Map.entry(CONTINENT_URI + "EUROPE", Map.of("no", "Europa")),
            Map.entry(CONTINENT_URI + "OCEANIA", Map.of("no", "Oseania")),
            Map.entry(CONTINENT_URI + "ANTARCTICA", Map.of("no", "Antarktika"))
    );

    public ContinentHarvester() {
        super();
    }

    public Model translateContinents(Model model) {
        Model translated = ModelFactory.createDefaultModel();
        model.listStatements().forEach(translated::add);

        for (String subject : missingTranslations.keySet()) {
            Resource subjectResource = model.getResource(subject);
            Map<String, String> subjectTranslations = missingTranslations.get(subject);
            for (Map.Entry<String, String> entry : subjectTranslations.entrySet()) {
                translated.add(
                        subjectResource,
                        DCTerms.title,
                        entry.getValue(),
                        entry.getKey()
                );
            }
        }

        updateModel(translated);
        return translated;
    }

    private Optional<Model> loadAndTranslateModel(org.springframework.core.io.Resource continentsRdfSource) {
        return loadModel(continentsRdfSource, false)
                .map(this::translateContinents);
    }

    public Flux<Continent> harvest() {
        log.info("Starting harvest of EU continents");
        final org.springframework.core.io.Resource continentsRdfSource = getSource();
        if(continentsRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch continents distribution"));
        }

        return Mono.justOrEmpty(loadAndTranslateModel(continentsRdfSource))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, DCTerms.Location).toList())
                .filter(Resource::isURIResource)
                .map(this::mapContinent);
    }

    private Continent mapContinent(Resource continent) {
        return Continent.builder()
                .uri(continent.getURI())
                .code(continent.getProperty(DCTerms.identifier).getObject().toString())
                .label(continent.listProperties(DCTerms.title).toList().stream()
                        .map(stmt -> stmt.getObject().asLiteral())
                        .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                        .collect(Collectors.toMap(Literal::getLanguage, Literal::getString)))
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                "PREFIX atres: <http://publications.europa.eu/resource/authority/>\n" +
                "CONSTRUCT {\n" +
                "  ?continent a dct:Location .\n" +
                "  ?continent dct:identifier ?code .\n" +
                "  ?continent dct:title ?prefLabel .\n" +
                "} WHERE {\n" +
                "  ?continent skos:inScheme atres:continent .\n" +
                "  ?continent a skos:Concept .\n" +
                "  ?continent dc:identifier ?code .\n" +
                "  FILTER(?code != 'OP_DATPRO') .\n" +
                "  ?continent skos:prefLabel ?prefLabel .\n" +
                "  FILTER(\n" +
                "    LANG(?prefLabel) = 'en' ||\n" +
                "    LANG(?prefLabel) = 'no' ||\n" +
                "    LANG(?prefLabel) = 'nb' ||\n" +
                "    LANG(?prefLabel) = 'nn'\n" +
                "  )\n" +
                "}";
        return URLEncoder.encode(query, StandardCharsets.UTF_8);
    }
}
