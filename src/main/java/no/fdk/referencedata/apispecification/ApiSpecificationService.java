package no.fdk.referencedata.apispecification;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.los.LosImporter;
import no.fdk.referencedata.los.LosNode;
import org.apache.jena.atlas.io.StringWriterI;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiSpecificationService {

    private List<ApiSpecification> allSpecifications = Collections.emptyList();

    public ApiSpecificationImporter apiSpecificationImporter;

    @Autowired
    public ApiSpecificationService(ApiSpecificationImporter apiSpecificationImporter) {
        this.apiSpecificationImporter = apiSpecificationImporter;
    }

    public List<ApiSpecification> getAll() {
        return allSpecifications;
    }

    public Optional<ApiSpecification> getByCode(final String code) {
        return allSpecifications.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        // Write a model in Turtle syntax, default style (pretty printed)
        RDFDataMgr.write(stringWriter, apiSpecificationImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importApiSpecifications() {
        log.debug("Importing api specifications");
        allSpecifications = apiSpecificationImporter.importFromSource();
    }

}
