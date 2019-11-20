package no.dcat.themes.service;

import jdk.nashorn.api.scripting.URLReader;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LosRDFImporter {

    private static final String defaultLanguage = "nb";
    private static final String LOS_HOME_ADRESS = "http://psi.norge.no/los/all.rdf";
    static private final Logger logger = LoggerFactory.getLogger(LosRDFImporter.class);

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
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    List<LosNode> importFromLosSource() {
        List<LosNode> allLosNodes = new ArrayList<>();


        final Model model = ModelFactory.createDefaultModel();

        URL losSourceURL;
        try {
            losSourceURL = new URL(LOS_HOME_ADRESS);
        } catch (MalformedURLException mue) {
            logger.error("Malformed LOS url: {}", LOS_HOME_ADRESS);
            return new ArrayList<>();
        }
        URLReader ur = new URLReader(losSourceURL);
        model.read(ur, losSourceURL.toString());

        ResIterator losIterator = model.listResourcesWithProperty(RDF.type, SKOS.Concept);

        while (losIterator.hasNext()) {
            Resource conceptResource = losIterator.nextResource();
            LosNode node = extractLosItemFromModel(conceptResource);
            allLosNodes.add(node);

        }

        //Secound pass - generate the paths.
        for (LosNode node : allLosNodes) {
            node.setLosPaths(generateLosPath(node, allLosNodes));
        }

        logger.debug("Got {} LOSes", allLosNodes.size());

        return allLosNodes;

    }

    private List<String> generateLosPath(LosNode node, List<LosNode> allLosNodes) {
        List<String> generatedPaths = new ArrayList<>();

        //Hovedkategori - /<keyword>
        if (node.getParents() == null || node.getParents().isEmpty()) {
            generatedPaths.add(getKeywordFromURI(node.getUri()).toLowerCase());
            return generatedPaths;
        }

        //Underkategori - /<hovedkategori>/<underkategori>
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {

            List<URI> hovedKategoriURIs = node.getParents();
            List<String> hovedKategoriPaths = new ArrayList<>();

            for (URI u : hovedKategoriURIs) {
                String subCategory = getKeywordFromURI(node.getUri());
                hovedKategoriPaths.add((getKeywordFromURI(u) + "/" + subCategory).toLowerCase());
            }
            return hovedKategoriPaths;
        }

        //Emneord - /<hovedkategori>/<underkatagori>/<emneord>
        List<String> allPaths = new ArrayList<>();

        List<URI> subCategoryURIs = node.getParents();
        for (URI subCategory : subCategoryURIs) {
            LosNode subCategoryLosnode = getByURI(subCategory, allLosNodes);
            List<URI> hovedCategories = subCategoryLosnode.getParents();
            for (URI hovedCategory : hovedCategories) {
                allPaths.add((getKeywordFromURI(hovedCategory) + "/" + getKeywordFromURI(subCategory) + "/" + getMostSaneName(node)).toLowerCase());
            }
        }
        return allPaths;
    }

    private LosNode getByURI(URI keyword, List<LosNode> allLosNodes) {
        String uriAsString = keyword.toString();
        for (LosNode node : allLosNodes) {
            if (node.getUri().equals(uriAsString)) {
                return node;
            }
        }
        return null;
    }
}
