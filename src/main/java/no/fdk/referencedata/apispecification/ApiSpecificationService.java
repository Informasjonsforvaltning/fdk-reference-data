package no.fdk.referencedata.apispecification;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.los.LosImporter;
import no.fdk.referencedata.los.LosNode;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.atlas.io.StringWriterI;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApiSpecificationService {
    private final String rdfSourceID = "api-specification-source";

    private List<ApiSpecification> allSpecifications = Collections.emptyList();

    public ApiSpecificationImporter apiSpecificationImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ApiSpecificationService(ApiSpecificationImporter apiSpecificationImporter, RDFSourceRepository rdfSourceRepository) {
        this.apiSpecificationImporter = apiSpecificationImporter;
        this.rdfSourceRepository = rdfSourceRepository;
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
        String source = rdfSourceRepository.findById(rdfSourceID).orElse(new RDFSource()).getTurtle();
        if (rdfFormat == RDFFormat.TURTLE) {
            return source;
        } else {
            return RDFUtils.modelToResponse(ModelFactory.createDefaultModel().read(source, Lang.TURTLE.getName()), rdfFormat);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importApiSpecifications() {
        log.debug("Importing api specifications");
        try {
            allSpecifications = apiSpecificationImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(apiSpecificationImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import api specifications", e);
        }
    }

}
