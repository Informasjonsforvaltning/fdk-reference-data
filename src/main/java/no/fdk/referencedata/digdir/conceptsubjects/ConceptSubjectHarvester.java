package no.fdk.referencedata.digdir.conceptsubjects;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.i18n.Language;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ConceptSubjectHarvester {

    private final ApplicationSettings applicationSettings;

    private static final List<String> SUPPORTED_LANGUAGES =
            Arrays.stream(Language.values())
                    .map(Language::code)
                    .toList();

    @Autowired
    public ConceptSubjectHarvester(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public String getVersion() {
        return "0";
    }

    public org.springframework.core.io.Resource getSource() {
        try {
            return new UrlResource(applicationSettings.getCatalogAdminUri() + "/concept-subjects");
        } catch (MalformedURLException e) {
            log.error("Unable to get source", e);
            return null;
        }
    }

    @Getter
    private final Model model = ModelFactory.createDefaultModel();

    private Optional<Model> fetchModel(org.springframework.core.io.Resource resource) {
        try {
            return Optional.of(RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE));
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    private void loadModel(org.springframework.core.io.Resource resource) {
        Optional<Model> fetched = fetchModel(resource);
        if (fetched.isPresent()) {
            model.removeAll();
            model.add(fetched.get());
        }
    }

    public Flux<ConceptSubject> harvest() {
        log.info("Starting harvest of concept subjects");
        final org.springframework.core.io.Resource rdfSource = getSource();
        if(rdfSource == null) {
            return Flux.error(new Exception("Unable to fetch concept subjects"));
        }

        loadModel(rdfSource);

        return Mono.justOrEmpty(model)
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, SKOS.Concept).toList())
                .filter(Resource::isURIResource)
                .map(this::mapConceptSubject);
    }

    private ConceptSubject mapConceptSubject(Resource conceptSubject) {
        final Map<String, String> label = new HashMap<>();
        Flux.fromIterable(conceptSubject.listProperties(SKOS.prefLabel).toList())
                .map(stmt -> stmt.getObject().asLiteral())
                .filter(literal -> SUPPORTED_LANGUAGES.contains(literal.getLanguage()))
                .doOnNext(literal -> label.put(literal.getLanguage(), literal.getString()))
                .subscribe();

        return ConceptSubject.builder()
                .uri(conceptSubject.getURI())
                .code(conceptSubject.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
