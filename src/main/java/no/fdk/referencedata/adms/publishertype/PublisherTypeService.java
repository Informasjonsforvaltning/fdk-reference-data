package no.fdk.referencedata.adms.publishertype;

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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PublisherTypeService {
    private final String rdfSourceID = "publisher-type-source";

    public PublisherTypeImporter publisherTypeImporter;

    private final PublisherTypeRepository publisherTypeRepository;

    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public PublisherTypeService(
            PublisherTypeImporter publisherTypeImporter,
            PublisherTypeRepository publisherTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.publisherTypeImporter = publisherTypeImporter;
        this.publisherTypeRepository = publisherTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
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
            publisherTypeRepository.deleteAll();
            publisherTypeRepository.saveAll(publisherTypes);

            RDFSource rdfSource = new RDFSource();
            rdfSource.setId(rdfSourceID);
            rdfSource.setTurtle(RDFUtils.modelToResponse(publisherTypeImporter.getModel(), RDFFormat.TURTLE));
            rdfSourceRepository.save(rdfSource);
        } catch(Exception e) {
            log.error("Unable to harvest adms publisher-types", e);
        }
    }

}
