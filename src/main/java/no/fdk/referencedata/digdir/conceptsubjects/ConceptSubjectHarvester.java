package no.fdk.referencedata.digdir.conceptsubjects;

import no.fdk.referencedata.rdf.SkosMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.ApplicationSettings;
import no.fdk.referencedata.core.HarvestParseException;
import no.fdk.referencedata.core.HarvestSourceException;
import no.fdk.referencedata.core.ModelHarvester;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
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
import java.util.Map;

@Component
@Slf4j
public class ConceptSubjectHarvester implements ModelHarvester<ConceptSubject> {

    private final ApplicationSettings applicationSettings;

    @Autowired
    public ConceptSubjectHarvester(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public org.springframework.core.io.Resource getSource() {
        try {
            return new UrlResource(applicationSettings.getCatalogAdminUri() + "/concept-subjects");
        } catch (MalformedURLException e) {
            throw new HarvestSourceException("Unable to get concept subjects source", e);
        }
    }

    @Getter
    private final Model model = ModelFactory.createDefaultModel();

    private Model fetchModel(org.springframework.core.io.Resource resource) {
        try {
            return RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE);
        } catch (IOException e) {
            throw new HarvestSourceException("Unable to load concept subjects model", e);
        } catch (RiotException e) {
            throw new HarvestParseException("Unable to parse concept subjects model", e);
        } catch (RuntimeException e) {
            throw new HarvestSourceException("Unable to load concept subjects model", e);
        }
    }

    private void loadModel(org.springframework.core.io.Resource resource) {
        Model fetched = fetchModel(resource);
        model.removeAll();
        model.add(fetched);
    }

    public Flux<ConceptSubject> harvest() {
        log.info("Starting harvest of concept subjects");
        final org.springframework.core.io.Resource rdfSource = getSource();

        loadModel(rdfSource);

        return Mono.justOrEmpty(model)
                .flatMapIterable(m -> m.listSubjectsWithProperty(RDF.type, SKOS.Concept).toList())
                .filter(Resource::isURIResource)
                .map(this::mapConceptSubject);
    }

    private ConceptSubject mapConceptSubject(Resource conceptSubject) {
        Map<String, String> label = SkosMapper.extractLabels(conceptSubject);

        return ConceptSubject.builder()
                .uri(conceptSubject.getURI())
                .code(conceptSubject.getProperty(DCTerms.identifier).getString().split("#")[1])
                .label(label)
                .build();
    }
}
