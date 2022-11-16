package no.fdk.referencedata.adms.publishertype;

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
public class PublisherTypeService {

    private List<PublisherType> publisherTypes = Collections.emptyList();

    public PublisherTypeImporter publisherTypeImporter;

    @Autowired
    public PublisherTypeService(PublisherTypeImporter publisherTypeImporter) {
        this.publisherTypeImporter = publisherTypeImporter;
    }

    public List<PublisherType> getAll() {
        return publisherTypes;
    }

    public Optional<PublisherType> getByCode(final String code) {
        return publisherTypes.stream()
                .filter(s -> s.code.equals(code))
                .findFirst();
    }

    public String getRdf(RDFFormat rdfFormat) {
        StringWriter stringWriter = new StringWriter();
        RDFDataMgr.write(stringWriter, publisherTypeImporter.getModel(), rdfFormat) ;
        return stringWriter.toString();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importPublisherTypes() {
        log.debug("Importing adms publisher-types");
        publisherTypes = publisherTypeImporter.importFromSource();
    }

}
