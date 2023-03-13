package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class LosImporter {

    private static final String defaultLanguage = "nb";

    List<LosNode> importFromLosSource() {
        final Model model = getModel();

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        // Extract the theme tree with words.
        List<LosNode> allLosNodes = concepts.stream()
                .filter(r -> isInScheme(r, LOS_ONTOLOGY.WORD) || isInScheme(r, LOS_ONTOLOGY.THEME))
                .map(LosImporter::extractLosItemFromModel)
                .sorted(Comparator.comparing(LosNode::getUri))
                .collect(Collectors.toList());

        // Generate the paths.
        for (LosNode node : allLosNodes) {
            node.setLosPaths(generateLosPaths(node, allLosNodes));
        }

        log.debug("Got {} LOSes", allLosNodes.size());

        return allLosNodes;

    }

    Model getModel() {
        return ModelFactory.createDefaultModel()
                .read(requireNonNull(LosImporter.class.getClassLoader().getResource("rdf/los.rdf"))
                .toString());
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

    private static String getKeywordFromURI(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private static List<String> generateLosPaths(LosNode node, List<LosNode> allLosNodes) {
        List<URI> parentURIs = node.getParents();
        String myKeyword = getKeywordFromURI(node.getUri());

        // no parents - root node has only self as the only element in the only path
        if (parentURIs == null || parentURIs.size() == 0) {
            return Collections.singletonList(myKeyword);
        }

        // add self to all parent paths
        return parentURIs.stream()
                .map(parentURI -> Optional.ofNullable(getByURI(parentURI, allLosNodes)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(parentNode -> generateLosPaths(parentNode, allLosNodes).stream())
                .map(parentPath -> parentPath + "/" + myKeyword)
                .collect(Collectors.toList());
    }

    private static LosNode getByURI(URI keyword, List<LosNode> allLosNodes) {
        String uriAsString = keyword.toString();
        for (LosNode node : allLosNodes) {
            if (node.getUri().equals(uriAsString)) {
                return node;
            }
        }
        return null;
    }

    private static class LOS_ONTOLOGY {
        static private final Resource WORD = ResourceFactory.createResource("https://psi.norge.no/los/ontologi/ord");
        static private final Resource THEME = ResourceFactory.createResource("https://psi.norge.no/los/ontologi/tema");
    }
}
