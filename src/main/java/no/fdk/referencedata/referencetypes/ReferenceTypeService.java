package no.fdk.referencedata.referencetypes;

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
public class ReferenceTypeService {
    private final String rdfSourceID = "reference-types-source";

    private List<ReferenceType> referenceTypes = Collections.emptyList();

    public ReferenceTypeImporter referenceTypeImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ReferenceTypeService(ReferenceTypeImporter referenceTypeImporter, RDFSourceRepository rdfSourceRepository) {
        this.referenceTypeImporter = referenceTypeImporter;
        this.rdfSourceRepository = rdfSourceRepository;
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
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importReferenceTypes() {
        log.debug("Importing reference types");
        try {
            referenceTypes = referenceTypeImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(referenceTypeImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import reference types", e);
        }
    }

}
