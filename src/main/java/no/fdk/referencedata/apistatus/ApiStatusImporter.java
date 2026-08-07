package no.fdk.referencedata.apistatus;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import no.fdk.referencedata.vocabulary.AT;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class ApiStatusImporter {


    private Model model;

    List<ApiStatus> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(ApiStatusImporter.class.getClassLoader().getResource("rdf/api-status.ttl"))
                .toString());

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        return concepts.stream()
                .map(ApiStatusImporter::extractApiStatusFromModel)
                .sorted(Comparator.comparing(ApiStatus::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static ApiStatus extractApiStatusFromModel(Resource specResource) {
        Map<String, String> label = SkosMapper.extractLabels(specResource);

        return ApiStatus.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(AT.authorityCode).getObject().toString())
                .label(label)
                .build();
    }
}
