package no.fdk.referencedata.apispecification;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.los.LosNode;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ApiSpecificationImporter {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private Model model;

    List<ApiSpecification> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(ApiSpecificationImporter.class.getClassLoader().getResource("rdf/api-specification-skos.ttl"))
                .toString());

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        // Extract the theme tree with words.
        return concepts.stream()
                .map(ApiSpecificationImporter::extractApiSpecificationFromModel)
                .sorted(Comparator.comparing(ApiSpecification::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static ApiSpecification extractApiSpecificationFromModel(Resource specResource) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(specResource.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ApiSpecification.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(DC.identifier).getObject().toString())
                .source(specResource.getProperty(DCTerms.source).getResource().getURI())
                .label(label)
                .build();
    }
}
