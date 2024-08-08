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

    public String genericSPARQLQuery(String schemaName) {
        return "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
                "PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
                "PREFIX atres: <http://publications.europa.eu/resource/authority/> " +
                "PREFIX at: <http://publications.europa.eu/ontology/authority/> " +
                "CONSTRUCT { " +
                    "atres:" + schemaName + " owl:versionInfo ?version . " +
                    "?item skos:inScheme atres:" + schemaName + " . " +
                    "?item dc:identifier ?code . " +
                    "?item at:start.use ?startUse . " +
                    "?item skos:prefLabel ?prefLabel . " +
                "} WHERE { " +
                    "atres:" + schemaName + " owl:versionInfo ?version . " +
                    "?item skos:inScheme atres:" + schemaName + " . " +
                    "?item a skos:Concept . " +
                    "?item dc:identifier ?code . " +
                    "FILTER(?code != 'OP_DATPRO') . " +
                    "OPTIONAL { ?item at:start.use ?startUse . } " +
                    "?item skos:prefLabel ?prefLabel . " +
                    "FILTER(" +
                        "LANG(?prefLabel) = 'en' || " +
                        "LANG(?prefLabel) = 'no' || " +
                        "LANG(?prefLabel) = 'nb' || " +
                        "LANG(?prefLabel) = 'nn'" +
                    ") . " +
                "}";
    }
}
