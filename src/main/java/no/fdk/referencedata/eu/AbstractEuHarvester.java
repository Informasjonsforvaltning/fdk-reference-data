package no.fdk.referencedata.eu;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.vocabulary.FDK;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Optional;

import static no.fdk.referencedata.rdf.RDFUtils.generateThemePaths;

@Component
@Slf4j
public abstract class AbstractEuHarvester<T> {
    private static final String SPARQL_API = "http://publications.europa.eu/webapi/rdf/sparql";

    public abstract String getVersion();

    private Model model = ModelFactory.createDefaultModel();

    public Resource getSource() {
        try {
            return new UrlResource(SPARQL_API + "?query=" + sparqlQuery());
        } catch (MalformedURLException e) {
            log.error("Unable to get source", e);
            return null;
        }
    }

    public Model getModel() { return model; }

    private void addThemePaths(Model m) {
        m.listResourcesWithProperty(RDF.type, SKOS.Concept).toList().stream()
                .flatMap(concept -> generateThemePaths(m, concept).stream().map(path -> Pair.of(concept, path)))
                .forEach(themeWithPath -> m.add(themeWithPath.getFirst(), FDK.themePath, themeWithPath.getSecond()));
    }

    protected Optional<Model> loadModel(Resource resource, boolean addEurovocPaths) {
        try {
            Model newModel = RDFDataMgr.loadModel(resource.getURI().toString(), Lang.TURTLE);
            if (addEurovocPaths) {
                addThemePaths(newModel);
            }
            model.removeAll().add(newModel);
            return Optional.of(model);
        } catch (IOException e) {
            log.error("Unable to load model", e);
            return Optional.empty();
        }
    }

    public abstract Flux<T> harvest();

    public abstract String sparqlQuery();
}
