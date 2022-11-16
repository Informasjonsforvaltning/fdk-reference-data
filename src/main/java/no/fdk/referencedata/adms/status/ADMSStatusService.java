package no.fdk.referencedata.adms.status;

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
public class ADMSStatusService {

    private List<ADMSStatus> admsStatuses = Collections.emptyList();

    public ADMSStatusImporter admsStatusImporter;

    @Autowired
    public ADMSStatusService(ADMSStatusImporter admsStatusImporter) {
        this.admsStatusImporter = admsStatusImporter;
    }

    public List<ADMSStatus> getAll() {
        return admsStatuses;
    }

    public Optional<ADMSStatus> getByCode(final String code) {
        return admsStatuses.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, admsStatusImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importADMSStatuses() {
        log.debug("Importing adms statuses");
        admsStatuses = admsStatusImporter.importFromSource();
    }

}
