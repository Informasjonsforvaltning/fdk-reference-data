package no.fdk.referencedata.schema.dayofweek;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SchemaDO;
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
public class DayOfWeekImporter {

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .collect(Collectors.toList());

    private Model model;

    List<DayOfWeek> importFromSource() {
        model = ModelFactory.createDefaultModel();
        model.read(requireNonNull(DayOfWeekImporter.class.getClassLoader().getResource("rdf/schema-day-of-week.ttl"))
                .toString());

        List<Resource> weekDays = model.listResourcesWithProperty(RDF.type, SchemaDO.DayOfWeek).toList();

        return weekDays.stream()
                .map(DayOfWeekImporter::extractDayOfWeekFromModel)
                .sorted(Comparator.comparing(DayOfWeek::getUri))
                .collect(Collectors.toList());
    }

    Model getModel() {
        return model;
    }

    private static DayOfWeek extractDayOfWeekFromModel(Resource specResource) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(specResource.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return DayOfWeek.builder()
                .uri(specResource.getURI())
                .code(specResource.getProperty(RDFS.label).getObject().toString())
                .label(label)
                .build();
    }
}
