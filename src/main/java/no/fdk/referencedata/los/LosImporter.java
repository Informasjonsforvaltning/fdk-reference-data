package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.vocabulary.FDK;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static no.fdk.referencedata.rdf.RDFUtils.generateThemePaths;

@Slf4j
@Service
public class LosImporter {
    private static final String LOS_URI = "https://psi.norge.no/los/all.rdf";

    private static final String defaultLanguage = "nb";

    private final Model model = ModelFactory.createDefaultModel();

    Model getModel() {
        return model;
    }

    public org.springframework.core.io.Resource getSource() {
        try {
            return new UrlResource(LOS_URI);
        } catch (MalformedURLException e) {
            log.error("Unable to get LOS source", e);
            return null;
        }
    }

    private void addLosPaths(Model m) {
        m.listResourcesWithProperty(RDF.type, SKOS.Concept).toList().stream()
                .flatMap(concept -> generateThemePaths(m, concept).stream().map(path -> Pair.of(concept, path)))
                .forEach(themeWithPath -> m.add(themeWithPath.getFirst(), FDK.themePath, themeWithPath.getSecond()));
    }

    private void loadModel(org.springframework.core.io.Resource resource) {
        try {
            Model newLosModel = RDFDataMgr.loadModel(resource.getURI().toString(), Lang.RDFXML);
            addLosPaths(newLosModel);
            model.removeAll().add(newLosModel);
        } catch (IOException e) {
            log.error("Unable to load LOS model", e);
        }
    }

    List<LosNode> importFromLosSource() {
        final org.springframework.core.io.Resource source = getSource();
        if (source != null) {
            loadModel(source);
        }

        List<Resource> concepts = getModel().listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        // Extract the theme tree with words.
        List<LosNode> allLosNodes = concepts.stream()
                .filter(r -> isInScheme(r, LOS_ONTOLOGY.WORD) || isInScheme(r, LOS_ONTOLOGY.THEME))
                .map(LosImporter::extractLosItemFromModel)
                .sorted(Comparator.comparing(LosNode::getUri))
                .collect(Collectors.toList());

        log.debug("Got {} LOSes", allLosNodes.size());

        return allLosNodes;

    }

    private static boolean isInScheme(Resource resource, Resource scheme) {
        return resource.hasProperty(SKOS.inScheme, scheme);
    }

    private static LosNode extractLosItemFromModel(Resource losResource) {
        LosNode node = new LosNode();
        node.setName(extractLanguageLiteral(losResource, SKOS.prefLabel));
        node.setDefinition(extractLanguageLiteral(losResource, SKOS.definition));
        node.setSynonyms(extractLanguageLiteralsOnlyValues(losResource, SKOS.hiddenLabel));
        node.setRelatedTerms(extractLiterals(losResource, SKOS.related));
        node.setParents(extractLiterals(losResource, SKOS.broader));
        node.setChildren(extractLiterals(losResource, SKOS.narrower));
        node.setTheme(requireNonNull(extractLiteral(losResource, SKOS.inScheme))
                .toString().equals(LosNode.NODE_IS_TEMA_OR_SUBTEMA));
        node.setUri(losResource.getURI());
        node.setLosPaths(extractStrings(losResource, FDK.themePath));
        return node;
    }

    private static Map<String, String> extractLanguageLiteral(Resource resource, Property property) {
        Map<String, String> map = new HashMap<>();

        StmtIterator iterator = resource.listProperties(property);

        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            String language = statement.getLanguage();
            if (language == null || language.isEmpty()) {
                language = defaultLanguage;
            }
            if (statement.getString() != null && !statement.getString().isEmpty()) {
                map.put(language, statement.getString());
            }
        }

        if (map.keySet().size() > 0) {
            return map;
        }

        return null;
    }

    private static List<String> extractLanguageLiteralsOnlyValues(Resource resource, Property property) {
        Map<String, String> map = new HashMap<>();

        StmtIterator iterator = resource.listProperties(property);

        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            String language = statement.getLanguage();
            if (language == null || language.isEmpty()) {
                language = defaultLanguage;
            }
            if (statement.getString() != null && !statement.getString().isEmpty()) {
                map.put(statement.getString(), language);
            }
        }
        return new ArrayList<>(map.keySet());
    }

    private static List<String> extractStrings(Resource resource, Property property) {
        Statement stmt = resource.getProperty(property);
        if (stmt == null) {
            return null;
        }
        return resource.listProperties(property).mapWith(Statement::getString).toList();
    }

    private static List<URI> extractLiterals(Resource resource, Property property) {
        List<URI> list = new ArrayList<>();
        Statement stmt = resource.getProperty(property);
        if (stmt == null) {
            return null;
        }
        StmtIterator iterator = resource.listProperties(property);

        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            if (statement.getResource() != null) {
                try {
                    list.add(new URI(statement.getResource().getURI()));
                } catch (URISyntaxException ue) {
                    log.warn("Got Exception for URI " + statement.getResource().getURI());
                }
            }
        }
        return list;
    }

    private static URI extractLiteral(Resource resource, Property property) {

        Statement stmt = resource.getProperty(property);
        if (stmt == null) {
            return null;
        }
        StmtIterator iterator = resource.listProperties(property);

        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            if (statement.getResource() != null) {
                try {
                    return new URI(statement.getResource().getURI());
                } catch (URISyntaxException ue) {
                    log.warn("Got Exception for URI " + statement.getResource().getURI());
                }
            }
        }
        return null;
    }

    private static class LOS_ONTOLOGY {
        static private final Resource WORD = ResourceFactory.createResource("https://psi.norge.no/los/ontologi/ord");
        static private final Resource THEME = ResourceFactory.createResource("https://psi.norge.no/los/ontologi/tema");
    }
}
