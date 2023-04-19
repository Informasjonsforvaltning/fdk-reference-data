package no.fdk.referencedata.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class FDK {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final String uri = "https://fellesdatakatalog.digdir.no/ontology/internal/";
    public static final Resource NAMESPACE;
    public static final Property themePath;

    static {
        NAMESPACE = m.createResource(uri);
        themePath = m.createProperty(uri + "themePath");
    }
}
