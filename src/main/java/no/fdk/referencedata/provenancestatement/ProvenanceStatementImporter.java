package no.fdk.referencedata.provenancestatement;

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
public class ProvenanceStatementImporter {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private Model model;

    List<ProvenanceStatement> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(ProvenanceStatementImporter.class.getClassLoader().getResource("rdf/provenance.ttl"))
                .toString());

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        return concepts.stream()
                .map(ProvenanceStatementImporter::extractProvenanceStatementFromModel)
                .sorted(Comparator.comparing(ProvenanceStatement::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static ProvenanceStatement extractProvenanceStatementFromModel(Resource specResource) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(specResource.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ProvenanceStatement.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(AT.authorityCode).getObject().toString())
                .label(label)
                .build();
    }
}
