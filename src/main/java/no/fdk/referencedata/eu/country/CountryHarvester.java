package no.fdk.referencedata.eu.country;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.AbstractEuHarvester;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Literal;
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
import java.util.stream.Collectors;

@Component
@Slf4j
public class CountryHarvester extends AbstractEuHarvester<Country> {

    public CountryHarvester() {
        super();
    }


    public Flux<Country> harvest() {
        log.info("Starting harvest of EU countries");
        final org.springframework.core.io.Resource fileTypesRdfSource = getSource();
        if(fileTypesRdfSource == null) {
            return Flux.error(new Exception("Unable to fetch country distribution"));
        }

        return Mono.justOrEmpty(loadModel(fileTypesRdfSource, false))
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, DCTerms.Location).toList())
                .filter(Resource::isURIResource)
                .map(this::mapCountry);
    }

    private Country mapCountry(Resource country) {
        Map<String, String> label = SkosMapper.extractLiteralProperty(country, DCTerms.title);

        return Country.builder()
                .uri(country.getURI())
                .code(country.getProperty(DCTerms.identifier).getObject().toString())
                .label(label)
                .build();
    }

    public String sparqlQuery() {
        String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
                "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                "PREFIX atres: <http://publications.europa.eu/resource/authority/>\n" +
                "CONSTRUCT {\n" +
                "  ?country a dct:Location .\n" +
                "  ?country dct:identifier ?code .\n" +
                "  ?country dct:title ?prefLabel .\n" +
                "} WHERE {\n" +
                "  ?country skos:inScheme atres:country .\n" +
                "  ?country a skos:Concept .\n" +
                "  ?country dc:identifier ?code .\n" +
                "  FILTER(?code != 'OP_DATPRO') .\n" +
                "  ?country skos:prefLabel ?prefLabel .\n" +
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
