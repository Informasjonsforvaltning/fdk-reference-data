package no.fdk.referencedata.eu.continent;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.fdk.referencedata.i18n.Language.*;

@Component
@Slf4j
public class ContinentHarvester extends AbstractEuHarvester<Continent> {
    private static String VERSION = "0";
    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    public ContinentHarvester() {
        super();
    }

    public String getVersion() {
        return VERSION;
    }

    public Flux<Continent> harvest() {
        log.info("Starting harvest of EU continents");
        final org.springframework.core.io.Resource continentsRdfSource = getSource();
        if(continentsRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch continents distribution"));
        }

        return Mono.justOrEmpty(loadModel(continentsRdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, DCTerms.Location).toList())
                .filter(Resource::isURIResource)
                .map(this::mapContinent);
    }

    private Continent mapContinent(Resource continent) {
        Map<String, String> label = continent.listProperties(DCTerms.title).toList().stream()
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .collect(Collectors.toMap(Literal::getLanguage, Literal::getString));

        String code = continent.getProperty(DCTerms.identifier).getObject().toString();

        switch (code) {
            case "AFRICA":
                label.put(NORWEGIAN.code(), "Afrika");
                break;
            case "AMERICA":
                label.put(NORWEGIAN.code(), "Amerika");
                break;
            case "ASIA":
                label.put(NORWEGIAN.code(), "Asia");
                break;
            case "EUROPE":
                label.put(NORWEGIAN.code(), "Europa");
                break;
            case "OCEANIA":
                label.put(NORWEGIAN.code(), "Oseania");
                break;
            case "ANTARCTICA":
                label.put(NORWEGIAN.code(), "Antarktika");
                break;
        }

        return Continent.builder()
                .uri(continent.getURI())
                .code(code)
                .label(label)
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
