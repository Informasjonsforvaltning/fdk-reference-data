package no.fdk.referencedata.adms.status;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
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
public class ADMSStatusImporter {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private Model model;

    List<ADMSStatus> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(ADMSStatusImporter.class.getClassLoader().getResource("rdf/adms-status.ttl"))
                .toString());

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        return concepts.stream()
                .map(ADMSStatusImporter::extractADMSStatusFromModel)
                .sorted(Comparator.comparing(ADMSStatus::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static ADMSStatus extractADMSStatusFromModel(Resource specResource) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(specResource.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ADMSStatus.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(SKOS.notation).getObject().toString())
                .label(label)
                .build();
    }
}
