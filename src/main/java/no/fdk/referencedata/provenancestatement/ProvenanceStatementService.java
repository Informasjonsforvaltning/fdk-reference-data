package no.fdk.referencedata.provenancestatement;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
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
    private final String rdfSourceID = "provenance-statements-source";

    private List<ProvenanceStatement> provenanceStatements = Collections.emptyList();

    public ProvenanceStatementImporter provenanceStatementImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ProvenanceStatementService(ProvenanceStatementImporter provenanceStatementImporter, RDFSourceRepository rdfSourceRepository) {
        this.provenanceStatementImporter = provenanceStatementImporter;
        this.rdfSourceRepository = rdfSourceRepository;
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
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importProvenanceStatements() {
        log.debug("Importing provenance statements");
        try {
            provenanceStatements = provenanceStatementImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(provenanceStatementImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import provenance statements", e);
        }
    }

}
