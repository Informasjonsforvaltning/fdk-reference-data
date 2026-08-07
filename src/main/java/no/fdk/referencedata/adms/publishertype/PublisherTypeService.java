package no.fdk.referencedata.adms.publishertype;

import no.fdk.referencedata.core.ReferenceDataWriter;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import no.fdk.referencedata.rdf.RDFUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
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
    private final RDFSourceRepository rdfSourceRepository;
    private final ReferenceDataWriter referenceDataWriter;
    public PublisherTypeImporter publisherTypeImporter;

    @Autowired
    public PublisherTypeService(
            PublisherTypeImporter publisherTypeImporter,
            PublisherTypeRepository publisherTypeRepository,
            RDFSourceRepository rdfSourceRepository,
            ReferenceDataWriter referenceDataWriter) {
        this.publisherTypeImporter = publisherTypeImporter;
        this.publisherTypeRepository = publisherTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
        this.referenceDataWriter = referenceDataWriter;
    }

    public List<PublisherType> getAll() {
        return publisherTypeRepository.findAll();
    }

    public Optional<PublisherType> getByCode(final String code) {
        return publisherTypeRepository.findByCode(code);
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
    public void importPublisherTypes() {
        log.debug("Importing adms publisher-types");
        try {
            final List<PublisherType> publisherTypes = publisherTypeImporter.importFromSource();

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(publisherTypeImporter.getModel(), RDFFormat.TURTLE));

            referenceDataWriter.replaceAll(publisherTypeRepository, publisherTypes, rdfSource);
        } catch (Exception e) {
            log.error("Unable to harvest adms publisher-types", e);
        }
    }

}
