package no.dcat.themes.service;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LosRDFImporter {

    private static final String defaultLanguage = "nb";
    static private final Logger logger = LoggerFactory.getLogger(LosRDFImporter.class);
    static private final String LOS_URL = LosRDFImporter.class.getClassLoader().getResource("rdf/los.rdf").toString();

    private static LosNode extractLosItemFromModel(Resource losResource) {
        LosNode node = new LosNode();
        node.setName(extractLanguageLiteral(losResource, SKOS.prefLabel));
        node.setDefinition(extractLanguageLiteral(losResource, SKOS.definition));
        node.setSynonyms(extractLanguageLiteralsOnlyValues(losResource, SKOS.hiddenLabel));
        node.setRelatedTerms(extractLiterals(losResource, SKOS.related));
        node.setParents(extractLiterals(losResource, SKOS.broader));
        node.setChildren(extractLiterals(losResource, SKOS.narrower));
        node.setTema(extractLiteral(losResource, SKOS.inScheme).toString().equals(LosNode.NODE_IS_TEMA_OR_SUBTEMA));
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
                    logger.warn("Got Exception for URI " + statement.getResource().getURI());
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
                    logger.warn("Got Exception for URI " + statement.getResource().getURI());
                }
            }
        }
        return null;
    }

    private static String getMostSaneName(LosNode node) {
        if (node.getName().containsKey("nb")) {
            return node.getName().get("nb");
        }
        if (node.getName().containsKey("nn")) {
            return node.getName().get("nn");
        }
        if (node.getName().containsKey("en")) {
            return node.getName().get("en");
        }
        return "";
    }

    private static String getKeywordFromURI(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private static String getKeywordFromURI(URI uri) {
        return getKeywordFromURI(uri.getPath());
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

    List<LosNode> importFromLosSource() {
        final Model model = ModelFactory.createDefaultModel();

        model.read(LOS_URL);

        List<Resource> concepts = model.listResourcesWithProperty(RDF.type, SKOS.Concept).toList();

        List<LosNode> allLosNodes = concepts.stream()
                .map(LosRDFImporter::extractLosItemFromModel)
                .collect(Collectors.toList());

        //Secound pass - generate the paths.
        for (LosNode node : allLosNodes) {
            node.setLosPaths(generateLosPaths(node, allLosNodes));
        }

        logger.debug("Got {} LOSes", allLosNodes.size());

        return allLosNodes;

    }
}
