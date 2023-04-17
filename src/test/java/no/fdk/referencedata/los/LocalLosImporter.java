package no.fdk.referencedata.los;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class LocalLosImporter extends LosImporter {

    public LocalLosImporter() {}

    @Override
    public Resource getSource() {
        return new ClassPathResource("los.rdf");
    }
}
