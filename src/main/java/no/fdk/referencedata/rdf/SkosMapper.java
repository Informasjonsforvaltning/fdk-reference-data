package no.fdk.referencedata.rdf;

import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SKOS;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SkosMapper {

    public static final List<String> SUPPORTED_LANGUAGE_CODES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private SkosMapper() {}

    public static Map<String, String> extractLabels(Resource concept) {
        return extractLiteralProperty(concept, SKOS.prefLabel);
    }

    public static Map<String, String> extractDefinitions(Resource concept) {
        return extractLiteralProperty(concept, SKOS.definition);
    }

    public static Map<String, String> extractLiteralProperty(Resource resource, Property property) {
        final Map<String, String> values = new HashMap<>();
        resource.listProperties(property).toList().stream()
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGE_CODES.contains(literal.getLanguage()))
                .forEach(literal -> values.put(literal.getLanguage(), literal.getString()));
        return values;
    }
}
