package no.fdk.referencedata.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class XKOS {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final String uri = "http://rdf-vocabulary.ddialliance.org/xkos#";
    public static final Resource NAMESPACE;
    public static final Property hasPart;
    public static final Property isPartOf;

    static {
        NAMESPACE = m.createResource(uri);
        hasPart = m.createProperty(uri + "hasPart");
        isPartOf = m.createProperty(uri + "isPartOf");
    }
}
