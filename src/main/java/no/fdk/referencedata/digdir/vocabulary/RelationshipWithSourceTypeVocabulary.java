package no.fdk.referencedata.digdir.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class RelationshipWithSourceTypeVocabulary {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Resource SCHEME;

    public static String getURI() {
        return "https://data.norge.no/vocabulary/relationship-with-source-type";
    }

    static {
        NAMESPACE = m.createResource(getURI() + "#");
        SCHEME = m.createResource(getURI());
    }
}
