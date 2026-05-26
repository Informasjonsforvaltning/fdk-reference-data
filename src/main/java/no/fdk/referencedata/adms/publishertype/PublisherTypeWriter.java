package no.fdk.referencedata.adms.publishertype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PublisherTypeWriter {

    private final PublisherTypeRepository publisherTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public PublisherTypeWriter(PublisherTypeRepository publisherTypeRepository, RDFSourceRepository rdfSourceRepository) {
        this.publisherTypeRepository = publisherTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<PublisherType> items, RDFSource rdfSource) {
        publisherTypeRepository.deleteAll();
        publisherTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
