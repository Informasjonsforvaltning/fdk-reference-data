package no.fdk.referencedata.apistatus;

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
public class ApiStatusService {

    private List<ApiStatus> apiStatuses = Collections.emptyList();

    public ApiStatusImporter apiStatusImporter;

    @Autowired
    public ApiStatusService(ApiStatusImporter apiStatusImporter) {
        this.apiStatusImporter = apiStatusImporter;
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
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, apiStatusImporter.getModel(), rdfFormat);
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importApiStatuses() {
        log.debug("Importing api statuses");
        apiStatuses = apiStatusImporter.importFromSource();
    }

}
