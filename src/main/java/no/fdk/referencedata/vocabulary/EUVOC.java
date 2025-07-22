package no.fdk.referencedata.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class EUVOC {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final String uri = "http://publications.europa.eu/ontology/euvoc#";
    public static final Resource NAMESPACE;
    public static final Property context;

    static {
        NAMESPACE = m.createResource(uri);
        context = m.createProperty(uri + "context");
    }
}
