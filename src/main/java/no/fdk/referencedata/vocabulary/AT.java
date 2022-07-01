package no.fdk.referencedata.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class AT {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final String uri = "http://publications.europa.eu/ontology/authority/";
    public static final Resource NAMESPACE;
    public static final Property authorityCode;

    static {
        NAMESPACE = m.createResource(uri);
        authorityCode = m.createProperty(uri + "authority-code");
    }
}
