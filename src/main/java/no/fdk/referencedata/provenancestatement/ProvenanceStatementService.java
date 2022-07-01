package no.fdk.referencedata.provenancestatement;

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
public class ProvenanceStatementService {

    private List<ProvenanceStatement> provenanceStatements = Collections.emptyList();

    public ProvenanceStatementImporter provenanceStatementImporter;

    @Autowired
    public ProvenanceStatementService(ProvenanceStatementImporter provenanceStatementImporter) {
        this.provenanceStatementImporter = provenanceStatementImporter;
    }

    public List<ProvenanceStatement> getAll() {
        return provenanceStatements;
    }

    public Optional<ProvenanceStatement> getByCode(final String code) {
        return provenanceStatements.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, provenanceStatementImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importProvenanceStatements() {
        log.debug("Importing provenance statements");
        provenanceStatements = provenanceStatementImporter.importFromSource();
    }

}
