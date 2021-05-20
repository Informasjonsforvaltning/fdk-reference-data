package no.fdk.referencedata.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class EUNotationType {
    private static final Model m = ModelFactory.createDefaultModel();
    public static final Resource NAMESPACE;
    public static final Resource IanaMT;

    public static String getURI() {
        return "http://publications.europa.eu/resource/authority/notation-type/";
    }

    static {
        NAMESPACE = m.createResource(getURI());
        IanaMT = m.createResource(getURI() + "IANA_MT");
    }
}
