package no.fdk.referencedata.eu.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class EUAuthorityOntology {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Property startUse;


    public static String getURI() {
        return "http://publications.europa.eu/ontology/authority/";
    }

    static {
        NAMESPACE = m.createResource(getURI());
        startUse = m.createProperty(getURI() + "start.use");
    }
}
