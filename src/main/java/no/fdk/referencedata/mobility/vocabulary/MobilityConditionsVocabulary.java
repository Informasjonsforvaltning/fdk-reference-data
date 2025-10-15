package no.fdk.referencedata.mobility.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class MobilityConditionsVocabulary {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Resource SCHEME;

    public static String getURI() {
        return "https://w3id.org/mobilitydcat-ap/conditions-for-access-and-usage";
    }

    static {
        NAMESPACE = m.createResource(getURI() + "/");
        SCHEME = m.createResource(getURI());
    }
}
