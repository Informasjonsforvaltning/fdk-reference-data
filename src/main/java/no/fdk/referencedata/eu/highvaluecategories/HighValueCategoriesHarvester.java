package no.fdk.referencedata.eu.highvaluecategories;

import no.fdk.referencedata.eu.GenericEuSkosHarvester;
import no.fdk.referencedata.rdf.SkosMapper;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class HighValueCategoriesHarvester extends GenericEuSkosHarvester<HighValueCategory> {

    private static final String SCHEMA_URI = "http://data.europa.eu/bna/asd487ae75";

    @Override
    protected String schemaName() {
        return "high-value-categories";
    }

    @Override
    protected Resource scheme() {
        return ResourceFactory.createResource(SCHEMA_URI);
    }

    @Override
    protected String logName() {
        return "high-value categories";
    }

    @Override
    protected HighValueCategory mapConcept(Resource highValueCategory) {
        return HighValueCategory.builder()
                .uri(highValueCategory.getURI())
                .code(extractCode(highValueCategory))
                .label(SkosMapper.extractLabels(highValueCategory))
                .build();
    }

    @Override
    public String sparqlQuery() {
        String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
            "PREFIX dct: <http://purl.org/dc/terms/> " +
            "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
            "PREFIX euvoc: <http://publications.europa.eu/ontology/euvoc#> " +
            "CONSTRUCT { " +
                "?category skos:inScheme <" + SCHEMA_URI + "> . " +
                "?category dc:identifier ?code . " +
                "?category skos:prefLabel ?prefLabel . " +
            "} WHERE { " +
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
