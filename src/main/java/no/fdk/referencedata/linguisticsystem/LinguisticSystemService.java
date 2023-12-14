package no.fdk.referencedata.linguisticsystem;

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
public class LinguisticSystemService {
    private final String rdfSourceID = "linguistic-system-source";

    private List<LinguisticSystem> linguisticSystems = Collections.emptyList();

    public LinguisticSystemImporter linguisticSystemImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public LinguisticSystemService(LinguisticSystemImporter linguisticSystemImporter, RDFSourceRepository rdfSourceRepository) {
        this.linguisticSystemImporter = linguisticSystemImporter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public List<LinguisticSystem> getAll() {
        return linguisticSystems;
    }

    public Optional<LinguisticSystem> getByCode(final String code) {
        return linguisticSystems.stream()
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
    public void importLinguisticSystems() {
        log.debug("Importing linguistic systems");
        try {
            linguisticSystems = linguisticSystemImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(linguisticSystemImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import linguistic systems", e);
        }
    }

}
