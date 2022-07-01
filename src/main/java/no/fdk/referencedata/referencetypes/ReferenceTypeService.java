package no.fdk.referencedata.referencetypes;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReferenceTypeService {

    private List<ReferenceType> referenceTypes = Collections.emptyList();

    public ReferenceTypeImporter referenceTypeImporter;

    @Autowired
    public ReferenceTypeService(ReferenceTypeImporter referenceTypeImporter) {
        this.referenceTypeImporter = referenceTypeImporter;
    }

    public List<ReferenceType> getAll() {
        return referenceTypes;
    }

    public Optional<ReferenceType> getByCode(final String code) {
        return referenceTypes.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, referenceTypeImporter.getModel(), rdfFormat);
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importReferenceTypes() {
        log.debug("Importing reference types");
        referenceTypes = referenceTypeImporter.importFromSource();
    }

}
