package no.fdk.referencedata.adms.publishertype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.core.ReferenceDataServiceSupport;
import org.apache.jena.riot.RDFFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PublisherTypeService {
    private final String rdfSourceID = "publisher-type-source";
    private final PublisherTypeRepository publisherTypeRepository;
    private final ReferenceDataServiceSupport support;
    public PublisherTypeImporter publisherTypeImporter;

    @Autowired
    public PublisherTypeService(
            PublisherTypeImporter publisherTypeImporter,
            PublisherTypeRepository publisherTypeRepository,
            ReferenceDataServiceSupport support) {
        this.publisherTypeImporter = publisherTypeImporter;
        this.publisherTypeRepository = publisherTypeRepository;
        this.support = support;
    }

    public List<PublisherType> getAll() {
        return publisherTypeRepository.findAll();
    }

    public Optional<PublisherType> getByCode(final String code) {
        return publisherTypeRepository.findByCode(code);
    }

    public String getRdf(RDFFormat rdfFormat) {
        return support.getRdf(rdfSourceID, rdfFormat);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importPublisherTypes() {
        log.debug("Importing adms publisher-types");
        try {
            final List<PublisherType> publisherTypes = publisherTypeImporter.importFromSource();
            support.saveAll(publisherTypes, publisherTypeImporter.getModel(), publisherTypeRepository, rdfSourceID);
        } catch (Exception e) {
            log.error("Unable to harvest adms publisher-types", e);
        }
    }
}
