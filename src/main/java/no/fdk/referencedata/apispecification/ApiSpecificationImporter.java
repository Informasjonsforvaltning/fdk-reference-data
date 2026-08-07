package no.fdk.referencedata.apispecification;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ApiSpecificationImporter {


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
        Map<String, String> label = SkosMapper.extractLabels(specResource);

        return ApiSpecification.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(DC.identifier).getObject().toString())
                .source(specResource.getProperty(DCTerms.source).getResource().getURI())
                .label(label)
                .build();
    }
}
