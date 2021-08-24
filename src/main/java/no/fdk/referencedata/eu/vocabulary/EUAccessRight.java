package no.fdk.referencedata.eu.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class EUAccessRight {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Resource SCHEME;

    public static String getURI() {
        return "http://publications.europa.eu/resource/authority/access-right";
    }

    static {
        NAMESPACE = m.createResource(getURI() + "/");
        SCHEME = m.createResource(getURI());
    }
}
