package no.fdk.referencedata.linguisticsystem;

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
public class LinguisticSystemImporter {


    private Model model;

    List<LinguisticSystem> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(LinguisticSystemImporter.class.getClassLoader().getResource("rdf/languages.ttl"))
                .toString());

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        return concepts.stream()
                .map(LinguisticSystemImporter::extractLinguisticSystemFromModel)
                .sorted(Comparator.comparing(LinguisticSystem::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static LinguisticSystem extractLinguisticSystemFromModel(Resource specResource) {
        Map<String, String> label = SkosMapper.extractLabels(specResource);

        return LinguisticSystem.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(AT.authorityCode).getObject().toString())
                .label(label)
                .build();
    }
}
