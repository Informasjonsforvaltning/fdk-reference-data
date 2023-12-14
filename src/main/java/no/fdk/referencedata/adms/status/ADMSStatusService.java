package no.fdk.referencedata.adms.status;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
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
public class ADMSStatusService {
    private final String rdfSourceID = "adms-status-source";

    private final ADMSStatusRepository admsStatusRepository;

    private final RDFSourceRepository rdfSourceRepository;

    public ADMSStatusImporter admsStatusImporter;

    @Autowired
    public ADMSStatusService(
            ADMSStatusImporter admsStatusImporter,
            ADMSStatusRepository admsStatusRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.admsStatusImporter = admsStatusImporter;
        this.admsStatusRepository = admsStatusRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    public List<ADMSStatus> getAll() {
        return admsStatusRepository.findAll();
    }

    public Optional<ADMSStatus> getByCode(final String code) {
        return admsStatusRepository.findByCode(code);
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, admsStatusImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importADMSStatuses() {
        log.debug("Importing adms statuses");
        try {
            final List<ADMSStatus> admsStatuses = admsStatusImporter.importFromSource();
            admsStatusRepository.deleteAll();
            admsStatusRepository.saveAll(admsStatuses);

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(admsStatusImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to harvest adms statuses", e);
        }
    }

}
