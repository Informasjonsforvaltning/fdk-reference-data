package no.fdk.referencedata.openlicences;

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
public class OpenLicenseService {

    private List<OpenLicense> openLicenses = Collections.emptyList();

    public OpenLicenseImporter openLicenseImporter;

    @Autowired
    public OpenLicenseService(OpenLicenseImporter openLicenseImporter) {
        this.openLicenseImporter = openLicenseImporter;
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
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, openLicenseImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importOpenLicenses() {
        log.debug("Importing open licenses");
        openLicenses = openLicenseImporter.importFromSource();
    }

}
