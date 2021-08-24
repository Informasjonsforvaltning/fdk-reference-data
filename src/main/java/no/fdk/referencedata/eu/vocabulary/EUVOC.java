package no.fdk.referencedata.eu.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class EUVOC {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Resource FileType;
    public static final Property xlNotation;
    public static final Property xlCodification;

    public static String getURI() {
        return "http://publications.europa.eu/ontology/euvoc#";
    }

    static {
        NAMESPACE = m.createResource(getURI());
        FileType = m.createResource(getURI() + "FileType");
        xlNotation = m.createProperty(getURI() + "xlNotation");
        xlCodification = m.createProperty(getURI() + "xlCodification");
    }
}
