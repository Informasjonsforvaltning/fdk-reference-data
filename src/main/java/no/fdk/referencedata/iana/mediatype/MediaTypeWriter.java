package no.fdk.referencedata.iana.mediatype;

import no.fdk.referencedata.rdf.RDFSource;
import no.fdk.referencedata.rdf.RDFSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MediaTypeWriter {

    private final MediaTypeRepository mediaTypeRepository;
    private final RDFSourceRepository rdfSourceRepository;

    @Autowired
    public MediaTypeWriter(
            MediaTypeRepository mediaTypeRepository,
            RDFSourceRepository rdfSourceRepository) {
        this.mediaTypeRepository = mediaTypeRepository;
        this.rdfSourceRepository = rdfSourceRepository;
    }

    @Transactional
    public void replaceAll(List<MediaType> items, RDFSource rdfSource) {
        mediaTypeRepository.deleteAll();
        mediaTypeRepository.saveAll(items);
        rdfSourceRepository.save(rdfSource);
    }
}
