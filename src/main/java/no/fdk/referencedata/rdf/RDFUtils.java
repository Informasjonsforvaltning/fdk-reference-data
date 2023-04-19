package no.fdk.referencedata.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.SKOS;
import org.springframework.data.util.Pair;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

public class RDFUtils {

    public static String modelToResponse(Model m, RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, m, rdfFormat) ;
        return stringWriter.toString();
    }

    private static String getKeywordFromURI(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    public static List<String> generateThemePaths(Model m, Resource theme) {
        String keyword = getKeywordFromURI(theme.getURI());
        List<Resource> parents = m.listObjectsOfProperty(theme, SKOS.broader)
                .mapWith(RDFNode::asResource)
                .toList();
        if (parents.isEmpty()) {
            return List.of(keyword);
        } else {
            return parents.stream()
                    .map(parent -> generateThemePaths(m, parent))
                    .flatMap(Collection::stream)
                    .map(parentPath -> parentPath + "/" + keyword)
                    .toList();
        }
    }

}
