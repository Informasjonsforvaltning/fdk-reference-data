package no.fdk.referencedata.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.StringWriter;

public class RDFUtils {

    public static String modelToResponse(Model m, RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, m, rdfFormat) ;
        return stringWriter.toString();
    }

}
