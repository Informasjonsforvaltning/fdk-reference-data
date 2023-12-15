package no.fdk.referencedata.apistatus;

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
public class ApiStatusService {
    private final String rdfSourceID = "api-status-source";

    private List<ApiStatus> apiStatuses = Collections.emptyList();

    public ApiStatusImporter apiStatusImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public ApiStatusService(ApiStatusImporter apiStatusImporter, RDFSourceRepository rdfSourceRepository) {
        this.apiStatusImporter = apiStatusImporter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public List<ApiStatus> getAll() {
        return apiStatuses;
    }

    public Optional<ApiStatus> getByCode(final String code) {
        return apiStatuses.stream()
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
    public void importApiStatuses() {
        log.debug("Importing api statuses");
        try {
            apiStatuses = apiStatusImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(apiStatusImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import api status", e);
        }
    }

}
