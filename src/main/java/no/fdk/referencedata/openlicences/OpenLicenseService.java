package no.fdk.referencedata.openlicences;

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
public class OpenLicenseService {
    private final String rdfSourceID = "open-licences-source";

    private List<OpenLicense> openLicenses = Collections.emptyList();

    public OpenLicenseImporter openLicenseImporter;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public OpenLicenseService(OpenLicenseImporter openLicenseImporter, RDFSourceRepository rdfSourceRepository) {
        this.openLicenseImporter = openLicenseImporter;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public List<OpenLicense> getAll() {
        return openLicenses;
    }

    public Optional<OpenLicense> getByCode(final String code) {
        return openLicenses.stream()
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
    public void importOpenLicenses() {
        log.debug("Importing open licenses");
        try {
            openLicenses = openLicenseImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(openLicenseImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to import open licenses", e);
        }
    }

}
